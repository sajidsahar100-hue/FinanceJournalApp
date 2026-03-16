package com.financeapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.financeapp.data.JournalEntry
import com.financeapp.data.Vendor
import com.financeapp.viewmodel.JournalViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryScreen(
    viewModel: JournalViewModel,
    entryId: Int,
    onBack: () -> Unit
) {
    val isEdit  = entryId != -1
    val vendors by viewModel.allVendors.observeAsState(emptyList())

    var date          by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var details       by remember { mutableStateOf("") }
    var amount        by remember { mutableStateOf("") }
    var type          by remember { mutableStateOf("CREDIT") }
    var selectedVendor by remember { mutableStateOf<Vendor?>(null) }
    var dropdownOpen  by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    var errDetails by remember { mutableStateOf(false) }
    var errAmount  by remember { mutableStateOf(false) }

    // Load existing entry when editing
    LaunchedEffect(entryId) {
        if (isEdit) {
            viewModel.getEntryById(entryId)?.let { e ->
                date    = e.date
                details = e.details
                amount  = e.amount.toString()
                type    = e.type
                selectedVendor = if (e.vendorId != null) vendors.find { it.id == e.vendorId } else null
            }
        }
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = runCatching {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)?.time
        }.getOrNull() ?: System.currentTimeMillis()
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit Entry" else "New Entry", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1B5E20), titleContentColor = Color.White
                )
            )
        }
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Date ─────────────────────────────────────────────
            OutlinedTextField(
                value = date,
                onValueChange = {},
                label = { Text("Date") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, "Pick date")
                    }
                }
            )

            // ── Details ───────────────────────────────────────────
            OutlinedTextField(
                value = details,
                onValueChange = { details = it; errDetails = false },
                label = { Text("Details / Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2, maxLines = 5,
                isError = errDetails,
                supportingText = if (errDetails) {{ Text("Description is required") }} else null
            )

            // ── Amount ────────────────────────────────────────────
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it; errAmount = false },
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = errAmount,
                supportingText = if (errAmount) {{ Text("Enter a valid positive amount") }} else null,
                leadingIcon = { Text("  \$") }
            )

            // ── Type ──────────────────────────────────────────────
            Text("Transaction Type", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("CREDIT", "DEBIT").forEach { t ->
                    FilterChip(
                        selected = type == t,
                        onClick  = { type = t },
                        label    = { Text(t) },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (t == "CREDIT") Color(0xFF43A047) else Color(0xFFE53935),
                            selectedLabelColor = Color.White
                        ),
                        leadingIcon = if (type == t) ({
                            Icon(Icons.Default.Check, null, Modifier.size(16.dp))
                        }) else null
                    )
                }
            }

            // ── Vendor ────────────────────────────────────────────
            Text("Vendor (Optional)", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
            ExposedDropdownMenuBox(expanded = dropdownOpen, onExpandedChange = { dropdownOpen = it }) {
                OutlinedTextField(
                    value = selectedVendor?.name ?: "None",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(dropdownOpen) }
                )
                ExposedDropdownMenu(dropdownOpen, { dropdownOpen = false }) {
                    DropdownMenuItem(
                        text = { Text("None") },
                        onClick = { selectedVendor = null; dropdownOpen = false }
                    )
                    vendors.forEach { v ->
                        DropdownMenuItem(
                            text = { Text(v.name) },
                            onClick = { selectedVendor = v; dropdownOpen = false }
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // ── Save ──────────────────────────────────────────────
            Button(
                modifier = Modifier.fillMaxWidth(),
                colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                onClick  = {
                    var ok = true
                    if (details.isBlank()) { errDetails = true; ok = false }
                    val amt = amount.toDoubleOrNull()
                    if (amt == null || amt <= 0) { errAmount = true; ok = false }
                    if (ok) {
                        val entry = JournalEntry(
                            id         = if (isEdit) entryId else 0,
                            date       = date,
                            details    = details.trim(),
                            amount     = amt!!,
                            type       = type,
                            vendorId   = selectedVendor?.id,
                            vendorName = selectedVendor?.name
                        )
                        if (isEdit) viewModel.updateEntry(entry)
                        else        viewModel.insertEntry(entry)
                        onBack()
                    }
                }
            ) {
                Icon(if (isEdit) Icons.Default.Save else Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text(if (isEdit) "Save Changes" else "Add Entry", modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}
