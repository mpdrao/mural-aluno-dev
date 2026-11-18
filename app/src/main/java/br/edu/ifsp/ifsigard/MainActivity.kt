package br.edu.ifsp.ifsigard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import br.edu.ifsp.ifsigard.data.local.AppDatabase
import br.edu.ifsp.ifsigard.data.repository.MuralRepository
import br.edu.ifsp.ifsigard.ui.navigation.AppNavGraph
import br.edu.ifsp.ifsigard.ui.theme.IFSIGARDTheme

class MainActivity : ComponentActivity() {

    private lateinit var repository: MuralRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        repository = MuralRepository(database)

        setContent {
            IFSIGARDTheme {
                AppNavGraph(repository = repository)
            }
        }
    }
}
