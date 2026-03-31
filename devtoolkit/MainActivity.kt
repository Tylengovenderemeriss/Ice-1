package com.example.devtoolkit

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext

data class DevNote(var content: String)
data class DevLink(var title: String, var url: String, var category: String)

class MainActivity : ComponentActivity() {

    private val notes = mutableStateListOf<DevNote>()
    private val links = mutableStateListOf<DevLink>()
    private val categories = listOf("Documentation", "Tutorials", "Tools", "GitHub", "Q&A / Stack Overflow")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DeveloperToolkitApp()
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DeveloperToolkitApp() {
        var selectedTab by remember { mutableIntStateOf(0) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Developer Toolkit") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    )
                )
            }
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) { Text("Device Info", modifier = Modifier.padding(16.dp)) }
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) { Text("Notes", modifier = Modifier.padding(16.dp)) }
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) { Text("Links", modifier = Modifier.padding(16.dp)) }
                }
                when (selectedTab) {
                    0 -> DeviceInfoScreen()
                    1 -> NotesScreen()
                    2 -> LinksScreen()
                }
            }
        }
    }

    @Composable
    fun DeviceInfoScreen() {
        val info = """
            Device Model: ${Build.MODEL}
            Manufacturer: ${Build.MANUFACTURER}
            Android Version: ${Build.VERSION.RELEASE}
            SDK Level: ${Build.VERSION.SDK_INT}
        """.trimIndent()

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = info, fontSize = 18.sp, style = MaterialTheme.typography.bodyLarge)
        }
    }

    @Composable
    fun NotesScreen() {
        var noteText by remember { mutableStateOf("") }

        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {

            OutlinedTextField(value = noteText, onValueChange = { noteText = it }, label = { Text("Enter Note") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                if (noteText.isNotBlank()) { notes.add(DevNote(noteText)); noteText = "" }
            }, modifier = Modifier.align(Alignment.End)) {
                Text("Add Note")
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn {
                itemsIndexed(notes) { index, note ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(note.content, modifier = Modifier.weight(1f))
                            IconButton(onClick = { notes.removeAt(index) }) { Text("Delete", color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun LinksScreen() {
        var titleText by remember { mutableStateOf("") }
        var urlText by remember { mutableStateOf("") }
        var selectedCategory by remember { mutableStateOf(categories[0]) }
        val context = LocalContext.current

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            OutlinedTextField(value = titleText, onValueChange = { titleText = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = urlText, onValueChange = { urlText = it }, label = { Text("URL") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            CategoryDropdown(categories = categories, selected = selectedCategory, onSelect = { selectedCategory = it })
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                if (titleText.isNotBlank() && urlText.isNotBlank()) {
                    val formattedUrl = if (!urlText.startsWith("http://") && !urlText.startsWith("https://")) "https://$urlText" else urlText
                    links.add(DevLink(titleText, formattedUrl, selectedCategory))
                    titleText = ""
                    urlText = ""
                }
            }, modifier = Modifier.align(Alignment.End)) { Text("Add Link") }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn {
                itemsIndexed(links) { index, link ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f).clickable {
                                try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link.url))) } catch (_: Exception) {}
                            }) {
                                Text(link.title, style = MaterialTheme.typography.titleMedium)
                                Text(link.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                Text(link.url, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                            }
                            IconButton(onClick = { links.removeAt(index) }) { Text("Delete", color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CategoryDropdown(categories: List<String>, selected: String, onSelect: (String) -> Unit) {
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(value = selected, onValueChange = {}, readOnly = true, label = { Text("Category") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                categories.forEach { category -> DropdownMenuItem(text = { Text(category) }, onClick = { onSelect(category); expanded = false }) }
            }
        }
    }
}