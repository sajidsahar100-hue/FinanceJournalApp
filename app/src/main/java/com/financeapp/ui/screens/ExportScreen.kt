package com.financeapp.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.financeapp.utils.ExcelExporter
import com.financeapp.utils.PdfExporter
import com.financeapp.viewmodel.JournalViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    viewModel: JournalViewModel,
    preVendorId: Int,
    preVendorName: String,
    onBack: () -> Unit
) {
    val context   = LocalContext.current
    val scope     = rememberCoroutineScope()
    val vendors   by viewModel.allVendors.observeAsState(emptyList())

    var mode           by remember { mutableStateOf(if (preVendorId != -1) "vendor" else "all") }
    var selVendorId    by remember { mutableStateOf(preVendorId) }
    var selVendorName  by remember { mutableStateOf(preVendorName) }
    var dropOpen       by remember { mutableStateOf(false) }
    var loading        by remember { mutableStateOf(false) }

    fun share(mimeType: String, block: suspend () -> java.io.File) {
        scope.launch {
            loading = true
            try {
                val file = block()
                val uri  = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
                context.startActivity(Intent.createChooser(
                    Intent(Intent.ACTION_SEND).apply {
                        type = mimeType
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }, "Share file"
                ))
            } catch (e: Exception) {
                Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_LONG).show()
            }
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Export", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1B5E20), titleContentColor = Color.White
                )
            )
        }
    ) { pad ->
        Column(
            Modifier.fillMaxSize().padding(pad).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Journal Scope", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(
                    selected = mode == "all",
                    onClick  = { mode = "all" },
                    label    = { Text("Main Journal (All)") },
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF1B5E20),
                        selectedLabelColor     = Color.White
                    )
                )
                FilterChip(
                    selected = mode == "vendor",
                    onClick  = { mode = "vendor" },
                    label    = { Text("By Vendor") },
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF1B5E20),
                        selectedLabelColor     = Color.White
                    )
                )
            }

            if (mode == "vendor") {
                ExposedDropdownMenuBox(dropOpen, { dropOpen = it }) {
                    OutlinedTextField(
                        value = if (selVendorId != -1) selVendorName else "Select a vendor…",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Vendor") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(dropOpen) }
                    )
                    ExposedDropdownMenu(dropOpen, { dropOpen = false }) {
                        vendors.forEach { v ->
                            DropdownMenuItem(
                                text = { Text(v.name) },
                                onClick = { selVendorId = v.id; selVendorName = v.name; dropOpen = false }
                            )
                        }
                    }
                }
            }

            Divider()
            Text("Format", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            val ready = !loading && (mode == "all" || selVendorId != -1)

            ExportBtn(
                label   = "Export to Excel  (.xls)",
                icon    = Icons.Default.TableChart,
                color   = Color(0xFF1565C0),
                enabled = ready
            ) {
                share("application/vnd.ms-excel") {
                    val list  = if (mode == "all") viewModel.getAllEntriesOnce()
                                else viewModel.getEntriesByVendorOnce(selVendorId)
                    val title = if (mode == "all") "Main Journal" else selVendorName
                    ExcelExporter.export(context, list, title)
                }
            }

            ExportBtn(
                label   = "Export to PDF",
                icon    = Icons.Default.PictureAsPdf,
                color   = Color(0xFFB71C1C),
                enabled = ready
            ) {
                share("application/pdf") {
                    val list  = if (mode == "all") viewModel.getAllEntriesOnce()
                                else viewModel.getEntriesByVendorOnce(selVendorId)
                    val title = if (mode == "all") "Main Journal" else selVendorName
                    PdfExporter.export(context, list, title)
                }
            }

            if (loading) {
                Box(Modifier.fillMaxWidth(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFF1B5E20))
                        Spacer(Modifier.height(8.dp))
                        Text("Generating file…", color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun ExportBtn(label: String, icon: ImageVector, color: Color, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick  = onClick,
        modifier = Modifier.fillMaxWidth(),
        enabled  = enabled,
        colors   = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Icon(icon, null)
        Spacer(Modifier.width(10.dp))
        Text(label, modifier = Modifier.padding(vertical = 4.dp))
    }
}
