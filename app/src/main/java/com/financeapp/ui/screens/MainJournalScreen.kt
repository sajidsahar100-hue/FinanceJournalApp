package com.financeapp.ui.screens

import androidx.compose.foundation.background
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
import com.financeapp.data.JournalEntry
import com.financeapp.utils.formatAmt
import com.financeapp.viewmodel.JournalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainJournalScreen(
    viewModel: JournalViewModel,
    onAddEntry: () -> Unit,
    onEditEntry: (Int) -> Unit,
    onVendors: () -> Unit,
    onExport: () -> Unit
) {
    val entries by viewModel.allEntries.observeAsState(emptyList())
    val credit  = entries.filter { it.type == "CREDIT" }.sumOf { it.amount }
    val debit   = entries.filter { it.type == "DEBIT"  }.sumOf { it.amount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Finance Journal", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1B5E20), titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = onVendors) {
                        Icon(Icons.Default.Business, "Vendors", tint = Color.White)
                    }
                    IconButton(onClick = onExport) {
                        Icon(Icons.Default.FileDownload, "Export", tint = Color.White)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddEntry, containerColor = Color(0xFF2E7D32)) {
                Icon(Icons.Default.Add, "Add Entry", tint = Color.White)
            }
        }
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad)) {

            // ── Balance card ──────────────────────────────────────
            Card(
                Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20))
            ) {
                Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Net Balance", color = Color.White.copy(.75f), fontSize = 13.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        formatAmt(credit - debit),
                        color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                        BalanceChip("Credit", "+${formatAmt(credit)}", Color(0xFFB9F6CA))
                        BalanceChip("Debit",  "-${formatAmt(debit)}",  Color(0xFFFFCDD2))
                    }
                }
            }

            // ── Entry list ────────────────────────────────────────
            if (entries.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AccountBalance, null,
                            Modifier.size(72.dp), tint = Color.Gray.copy(.35f))
                        Spacer(Modifier.height(16.dp))
                        Text("No entries yet", color = Color.Gray)
                        Text("Tap + to add your first entry", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text("All Entries (${entries.size})",
                            fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 4.dp))
                    }
                    items(entries, key = { it.id }) { entry ->
                        EntryCard(
                            entry    = entry,
                            onEdit   = { onEditEntry(entry.id) },
                            onDelete = { viewModel.deleteEntry(entry) }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
fun BalanceChip(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.White.copy(.7f), fontSize = 11.sp)
        Text(value, color = color, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

@Composable
fun EntryCard(entry: JournalEntry, onEdit: () -> Unit, onDelete: () -> Unit) {
    var confirmDelete by remember { mutableStateOf(false) }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete Entry") },
            text  = { Text("Remove this entry permanently?") },
            confirmButton = {
                TextButton(onClick = { onDelete(); confirmDelete = false }) {
                    Text("Delete", color = Color(0xFFC62828))
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }

    val isCredit = entry.type == "CREDIT"
    Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(46.dp)
                    .background(
                        if (isCredit) Color(0xFF43A047) else Color(0xFFE53935),
                        MaterialTheme.shapes.small
                    ),
                Alignment.Center
            ) {
                Icon(
                    if (isCredit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    null, tint = Color.White, modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(entry.details, fontWeight = FontWeight.SemiBold, maxLines = 2)
                Text(entry.date, fontSize = 12.sp, color = Color.Gray)
                if (!entry.vendorName.isNullOrEmpty())
                    Text(entry.vendorName, fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Medium)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${if (isCredit) "+" else "-"}${formatAmt(entry.amount)}",
                    fontWeight = FontWeight.Bold,
                    color = if (isCredit) Color(0xFF2E7D32) else Color(0xFFC62828),
                    fontSize = 15.sp
                )
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Edit, null, Modifier.size(15.dp))
                    }
                    IconButton(onClick = { confirmDelete = true }, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Delete, null, Modifier.size(15.dp), tint = Color(0xFFC62828))
                    }
                }
            }
        }
    }
}
