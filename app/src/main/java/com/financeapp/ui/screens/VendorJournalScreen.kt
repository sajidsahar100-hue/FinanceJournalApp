package com.financeapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.financeapp.utils.formatAmt
import com.financeapp.viewmodel.JournalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorJournalScreen(
    viewModel: JournalViewModel,
    vendorId: Int,
    vendorName: String,
    onBack: () -> Unit,
    onExport: (Int, String) -> Unit
) {
    val entries by viewModel.getEntriesByVendor(vendorId)
        .collectAsState(initial = emptyList())

    val credit  = entries.filter { it.type == "CREDIT" }.sumOf { it.amount }
    val debit   = entries.filter { it.type == "DEBIT"  }.sumOf { it.amount }
    val balance = credit - debit

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(vendorName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1B5E20), titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { onExport(vendorId, vendorName) }) {
                        Icon(Icons.Default.FileDownload, "Export", tint = Color.White)
                    }
                }
            )
        }
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad)) {
            // Summary row
            Card(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32))
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    Arrangement.SpaceEvenly, Alignment.CenterVertically
                ) {
                    VendorStat("Balance",  formatAmt(balance), Color.White)
                    Divider(Modifier.height(32.dp).width(1.dp), color = Color.White.copy(.3f))
                    VendorStat("Credit", "+${formatAmt(credit)}", Color(0xFFB9F6CA))
                    Divider(Modifier.height(32.dp).width(1.dp), color = Color.White.copy(.3f))
                    VendorStat("Debit",  "-${formatAmt(debit)}",  Color(0xFFFFCDD2))
                }
            }

            if (entries.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("No entries for $vendorName", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text("${entries.size} entries", fontSize = 13.sp, color = Color.Gray,
                            modifier = Modifier.padding(bottom = 4.dp))
                    }
                    items(entries, key = { it.id }) { entry ->
                        EntryCard(entry, onEdit = {}, onDelete = { viewModel.deleteEntry(entry) })
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
fun VendorStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.White.copy(.7f), fontSize = 11.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}
