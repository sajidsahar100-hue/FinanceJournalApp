package com.financeapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.financeapp.data.Vendor
import com.financeapp.utils.formatAmt
import com.financeapp.viewmodel.JournalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorsScreen(
    viewModel: JournalViewModel,
    onBack: () -> Unit,
    onVendorClick: (Int, String) -> Unit
) {
    val vendors by viewModel.allVendors.observeAsState(emptyList())
    val entries by viewModel.allEntries.observeAsState(emptyList())
    var showAdd     by remember { mutableStateOf(false) }
    var newName     by remember { mutableStateOf("") }
    var nameError   by remember { mutableStateOf(false) }

    if (showAdd) {
        AlertDialog(
            onDismissRequest = { showAdd = false; newName = "" },
            title = { Text("Add Vendor") },
            text  = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it; nameError = false },
                    label = { Text("Vendor name") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = nameError,
                    supportingText = if (nameError) {{ Text("Name required") }} else null
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newName.isBlank()) nameError = true
                    else { viewModel.addVendor(newName.trim()); newName = ""; showAdd = false }
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false; newName = "" }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vendors", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1B5E20), titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }, containerColor = Color(0xFF2E7D32)) {
                Icon(Icons.Default.Add, "Add Vendor", tint = Color.White)
            }
        }
    ) { pad ->
        if (vendors.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(pad), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Business, null, Modifier.size(72.dp), tint = Color.Gray.copy(.35f))
                    Spacer(Modifier.height(16.dp))
                    Text("No vendors yet", color = Color.Gray)
                    Text("Tap + to add one", color = Color.Gray, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(pad),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(vendors, key = { it.id }) { vendor ->
                    val ve = entries.filter { it.vendorId == vendor.id }
                    VendorCard(
                        vendor   = vendor,
                        count    = ve.size,
                        balance  = ve.sumOf { if (it.type == "CREDIT") it.amount else -it.amount },
                        onClick  = { onVendorClick(vendor.id, vendor.name) },
                        onDelete = { viewModel.deleteVendor(vendor) }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun VendorCard(vendor: Vendor, count: Int, balance: Double, onClick: () -> Unit, onDelete: () -> Unit) {
    var confirm by remember { mutableStateOf(false) }
    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("Delete Vendor") },
            text  = { Text("Delete ${vendor.name}? Existing journal entries will remain.") },
            confirmButton = {
                TextButton(onClick = { onDelete(); confirm = false }) {
                    Text("Delete", color = Color(0xFFC62828))
                }
            },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text("Cancel") } }
        )
    }
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Business, null, Modifier.size(40.dp), tint = Color(0xFF1B5E20))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(vendor.name, fontWeight = FontWeight.SemiBold)
                Text("$count entries", fontSize = 12.sp, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    formatAmt(balance),
                    fontWeight = FontWeight.Bold,
                    color = if (balance >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("View →", fontSize = 11.sp, color = Color(0xFF1B5E20))
                    Spacer(Modifier.width(4.dp))
                    IconButton(onClick = { confirm = true }, Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, null, Modifier.size(14.dp), tint = Color(0xFFC62828))
                    }
                }
            }
        }
    }
}
