package com.financeapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.*
import androidx.navigation.compose.*
import com.financeapp.ui.screens.*
import com.financeapp.viewmodel.JournalViewModel

@Composable
fun AppNavigation(navController: NavHostController, viewModel: JournalViewModel) {
    NavHost(navController, startDestination = "main") {

        composable("main") {
            MainJournalScreen(
                viewModel   = viewModel,
                onAddEntry  = { navController.navigate("add_entry/-1") },
                onEditEntry = { id -> navController.navigate("add_entry/$id") },
                onVendors   = { navController.navigate("vendors") },
                onExport    = { navController.navigate("export/-1/Main Journal") }
            )
        }

        composable(
            "add_entry/{entryId}",
            arguments = listOf(navArgument("entryId") { type = NavType.IntType })
        ) { back ->
            AddEntryScreen(
                viewModel = viewModel,
                entryId   = back.arguments?.getInt("entryId") ?: -1,
                onBack    = { navController.popBackStack() }
            )
        }

        composable("vendors") {
            VendorsScreen(
                viewModel      = viewModel,
                onBack         = { navController.popBackStack() },
                onVendorClick  = { id, name ->
                    navController.navigate("vendor_journal/$id/${name.replace("/","_")}")
                }
            )
        }

        composable(
            "vendor_journal/{vendorId}/{vendorName}",
            arguments = listOf(
                navArgument("vendorId")   { type = NavType.IntType },
                navArgument("vendorName") { type = NavType.StringType }
            )
        ) { back ->
            VendorJournalScreen(
                viewModel  = viewModel,
                vendorId   = back.arguments?.getInt("vendorId")     ?: 0,
                vendorName = back.arguments?.getString("vendorName") ?: "",
                onBack     = { navController.popBackStack() },
                onExport   = { id, name ->
                    navController.navigate("export/$id/${name.replace("/","_")}")
                }
            )
        }

        composable(
            "export/{vendorId}/{vendorName}",
            arguments = listOf(
                navArgument("vendorId")   { type = NavType.IntType },
                navArgument("vendorName") { type = NavType.StringType }
            )
        ) { back ->
            ExportScreen(
                viewModel           = viewModel,
                preVendorId         = back.arguments?.getInt("vendorId")     ?: -1,
                preVendorName       = back.arguments?.getString("vendorName") ?: "",
                onBack              = { navController.popBackStack() }
            )
        }
    }
}
