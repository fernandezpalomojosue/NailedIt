package com.josuefp.servicenotebook.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.josuefp.servicenotebook.data.local.AppDatabase
import com.josuefp.servicenotebook.data.local.entity.WorkPhotoEntity
import com.josuefp.servicenotebook.data.repository.GalleryRepository
import com.josuefp.servicenotebook.ui.theme.ServiceNotebookTheme
import com.josuefp.servicenotebook.ui.viewmodel.GalleryTab
import com.josuefp.servicenotebook.ui.viewmodel.GalleryViewModel
import com.josuefp.servicenotebook.ui.viewmodel.GalleryViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GalleryScreen(
    appName: String = "Virna Nails",
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { GalleryRepository(database) }
    val viewModel: GalleryViewModel = viewModel(factory = GalleryViewModelFactory(repository))

    val selectedTab by viewModel.selectedTab.collectAsState()
    val photos by viewModel.photos.collectAsState()

    var showAddPhotoDialog by remember { mutableStateOf(false) }
    var selectedPhotoForDetail by remember { mutableStateOf<WorkPhotoEntity?>(null) }
    var photoToDelete by remember { mutableStateOf<WorkPhotoEntity?>(null) }

    GalleryScreenContent(
        appName = appName,
        selectedTab = selectedTab,
        photos = photos,
        onTabSelected = viewModel::setSelectedTab,
        onNavigateBack = onNavigateBack,
        onAddPhotoClick = { showAddPhotoDialog = true },
        onPhotoClick = { photo -> selectedPhotoForDetail = photo }
    )

    if (showAddPhotoDialog) {
        AddWorkPhotoDialog(
            initialCategory = if (selectedTab == GalleryTab.HANDS) "Manos" else "Pies",
            onDismiss = { showAddPhotoDialog = false },
            onConfirm = { uri, category, description ->
                viewModel.addPhoto(context, uri, category, description) {
                    showAddPhotoDialog = false
                }
            }
        )
    }

    if (selectedPhotoForDetail != null) {
        WorkPhotoDetailDialog(
            photo = selectedPhotoForDetail!!,
            onDismiss = { selectedPhotoForDetail = null },
            onDelete = { photo ->
                photoToDelete = photo
                selectedPhotoForDetail = null
            }
        )
    }

    if (photoToDelete != null) {
        AlertDialog(
            onDismissRequest = { photoToDelete = null },
            title = { Text("Eliminar foto") },
            text = { Text("¿Deseas eliminar esta imagen de la galería?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePhoto(context, photoToDelete!!)
                        photoToDelete = null
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { photoToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreenContent(
    appName: String,
    selectedTab: GalleryTab,
    photos: List<WorkPhotoEntity>,
    onTabSelected: (GalleryTab) -> Unit,
    onNavigateBack: () -> Unit,
    onAddPhotoClick: () -> Unit,
    onPhotoClick: (WorkPhotoEntity) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$appName - Galería") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddPhotoClick) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Foto")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PrimaryTabRow(
                selectedTabIndex = selectedTab.ordinal
            ) {
                Tab(
                    selected = selectedTab == GalleryTab.HANDS,
                    onClick = { onTabSelected(GalleryTab.HANDS) },
                    text = { Text("Manos 💅") }
                )
                Tab(
                    selected = selectedTab == GalleryTab.FEET,
                    onClick = { onTabSelected(GalleryTab.FEET) },
                    text = { Text("Pies 🦶") }
                )
            }

            if (photos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay fotos registradas en esta sección. Presiona la cámara para agregar la primera.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(photos, key = { it.id }) { photo ->
                        WorkPhotoCard(
                            photo = photo,
                            onClick = { onPhotoClick(photo) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WorkPhotoCard(
    photo: WorkPhotoEntity,
    onClick: () -> Unit
) {
    val bitmap = remember(photo.imagePath) {
        try {
            BitmapFactory.decodeFile(photo.imagePath)?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color.Gray.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = photo.description,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (photo.description.isNotBlank()) {
                Text(
                    text = photo.description,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun AddWorkPhotoDialog(
    initialCategory: String,
    onDismiss: () -> Unit,
    onConfirm: (uri: Uri, category: String, description: String) -> Unit
) {
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var category by remember { mutableStateOf(initialCategory) }
    var description by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            errorMessage = null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Trabajo a Galería") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (errorMessage != null) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Button(
                    onClick = { photoPickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (selectedUri == null) "Seleccionar Foto de Galería" else "Cambiar Foto")
                }

                if (selectedUri != null) {
                    Text("✓ Foto seleccionada", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }

                Text("Sección / Categoría *", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = category == "Manos",
                        onClick = { category = "Manos" },
                        label = { Text("Manos 💅") }
                    )
                    FilterChip(
                        selected = category == "Pies",
                        onClick = { category = "Pies" },
                        label = { Text("Pies 🦶") }
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (ej. Esculpido Gel tono Nude)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val uri = selectedUri
                    if (uri == null) {
                        errorMessage = "Debes seleccionar una imagen."
                    } else {
                        onConfirm(uri, category, description)
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun WorkPhotoDetailDialog(
    photo: WorkPhotoEntity,
    onDismiss: () -> Unit,
    onDelete: (WorkPhotoEntity) -> Unit
) {
    val bitmap = remember(photo.imagePath) {
        try {
            BitmapFactory.decodeFile(photo.imagePath)?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detalle del Trabajo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(Color.Black, shape = MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = photo.description,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text("No se pudo cargar la imagen", color = Color.White)
                    }
                }

                Text(
                    text = "Sección: ${photo.category}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                if (photo.description.isNotBlank()) {
                    Text(
                        text = photo.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Text(
                    text = "Fecha: ${dateFormat.format(Date(photo.createdAt))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onDelete(photo) },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun GalleryScreenPreview() {
    ServiceNotebookTheme {
        GalleryScreenContent(
            appName = "Virna Nails",
            selectedTab = GalleryTab.HANDS,
            photos = emptyList(),
            onTabSelected = {},
            onNavigateBack = {},
            onAddPhotoClick = {},
            onPhotoClick = {}
        )
    }
}
