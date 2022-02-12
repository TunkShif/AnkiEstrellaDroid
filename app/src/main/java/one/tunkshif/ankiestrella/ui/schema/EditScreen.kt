package one.tunkshif.ankiestrella.ui.schema

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import one.tunkshif.ankiestrella.R
import one.tunkshif.ankiestrella.data.dao.SchemaDao
import one.tunkshif.ankiestrella.ui.composable.AutoCompleteTextField
import one.tunkshif.ankiestrella.ui.composable.TextFieldState
import one.tunkshif.ankiestrella.ui.theme.*
import one.tunkshif.ankiestrella.util.TAG
import org.koin.androidx.compose.inject

@Composable
fun EditScreen(
    viewModel: EditSchemaViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val uiState = remember { viewModel.uiState }
    val inputState = remember { viewModel.inputState }

    val onSave: () -> Unit = {
        coroutineScope.launch {
            when {
                !inputState.isValid -> {
                    Toast.makeText(context, "Please fill in required fields.", Toast.LENGTH_SHORT)
                        .show()
                }
                !viewModel.hasConflict() -> {
                    viewModel.save()
                    Toast.makeText(context, "Schema saved!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    Scaffold(
        topBar = { EditScreenTopAppBar() },
        floatingActionButton = { EditScreenFloatingActionButton(onClick = onSave) }
    ) {
        EditScreenContent(
            uiState = uiState,
            inputState = inputState,
        )
        if (uiState.hasConflict) {
            OnSaveAlertDialog(
                schemaName = inputState.schemaNameState.text,
                onDismiss = { uiState.hasConflict = false },
                onConfirm = {
                    viewModel.update()
                    uiState.hasConflict = false
                },
                onCancel = { uiState.hasConflict = false }
            )
        }
    }
}

@Composable
fun EditScreenTopAppBar() {
    TopAppBar(
        title = { Text(text = "Edit Schema") },
        navigationIcon = {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_left_outline),
                    contentDescription = "back"
                )
            }
        },
        actions = {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_dots_vertical_outline),
                    contentDescription = "menu"
                )
            }
        }
    )
}

@Composable
fun EditScreenFloatingActionButton(
    onClick: () -> Unit
) {
    FloatingActionButton(
        backgroundColor = AnkiBlue200,
        contentColor = White.copy(alpha = 0.95f),
        onClick = onClick
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_check_outline),
            contentDescription = "finish"
        )
    }
}

@Composable
fun EditScreenContent(
    uiState: EditUiState,
    inputState: EditInputState
) {
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimension.small, Dimension.extraSmall, Dimension.small, Dimension.none)
            .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimension.small),
            modifier = Modifier.fillMaxWidth()
        ) {
            SchemaEditSection(
                decks = uiState.decks,
                sources = uiState.sources,
                models = uiState.models,
                schemaNameState = inputState.schemaNameState,
                deckName = inputState.deckName,
                sourceName = inputState.sourceName,
                modelName = inputState.modelName,
                onDeckSelected = { inputState.deckName = it },
                onSourceSelected = {
                    inputState.sourceName = it
                    uiState.onSourceSelected(it)
                },
                onModelSelected = {
                    inputState.modelName = it
                    uiState.onModelSelected(it)
                }
            )
            if (uiState.isModelSelected) {
                FieldMappingSection(
                    modelFields = uiState.modelFields,
                    sourceFields = uiState.sourceFields,
                    fieldMapping = inputState.fieldMapping,
                    onFieldMappingChange = inputState::onFieldMappingChange
                )
            }
        }
    }
}

@Composable
fun SchemaEditSection(
    schemaNameState: TextFieldState,
    decks: List<String>,
    sources: List<String>,
    models: List<String>,
    deckName: String,
    sourceName: String,
    modelName: String,
    onDeckSelected: (String) -> Unit,
    onSourceSelected: (String) -> Unit,
    onModelSelected: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Dimension.tiny),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val focusManager = LocalFocusManager.current

        SchemaInput(
            schemaNameState,
            onImeAction = { focusManager.clearFocus() }
        )
        DeckSelect(
            value = deckName,
            items = decks,
            onItemSelected = onDeckSelected
        )
        SourceSelect(
            value = sourceName,
            items = sources,
            onItemSelected = onSourceSelected
        )
        ModelSelect(
            value = modelName,
            items = models,
            onItemSelected = onModelSelected
        )
    }
}

@Composable
fun SchemaInput(
    textState: TextFieldState,
    onImeAction: () -> Unit = {}
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Dimension.tiny)
    ) {
        OutlinedTextField(
            value = textState.text,
            onValueChange = { textState.text = it },
            shape = Rounded.large,
            singleLine = true,
            label = { Text(text = "Name") },
            placeholder = { Text(text = "Schema name") },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_document_solid),
                    contentDescription = "schema name"
                )
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onImeAction() }),
            isError = textState.shouldShowError,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged {
                    val focused = it.isFocused
                    textState.onFocusChange(focused)
                    if (!focused) textState.enableShowError()
                }
        )
        textState.getError()?.let {
            Text(
                text = it,
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.caption
            )
        }
    }
}

@Composable
fun DeckSelect(
    value: String,
    items: List<String>,
    onItemSelected: (String) -> Unit,
) {
    AutoCompleteTextField(
        value = value,
        onItemSelected = onItemSelected,
        items = items,
        shape = Rounded.large,
        label = { Text(text = "Deck") },
        placeholder = { Text(text = "Deck name") },
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_collection_solid),
                contentDescription = "deck name"
            )
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun SourceSelect(
    value: String,
    items: List<String>,
    onItemSelected: (String) -> Unit,
) {
    AutoCompleteTextField(
        value = value,
        onItemSelected = onItemSelected,
        items = items,
        shape = Rounded.large,
        label = { Text(text = "Dict") },
        placeholder = { Text(text = "Dict name") },
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_book_open_solid),
                contentDescription = "dict name"
            )
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun ModelSelect(
    value: String,
    items: List<String>,
    onItemSelected: (String) -> Unit,
) {
    AutoCompleteTextField(
        value = value,
        onItemSelected = onItemSelected,
        items = items,
        shape = Rounded.large,
        label = { Text(text = "Model") },
        placeholder = { Text(text = "Model Name") },
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_clipboard_solid),
                contentDescription = "model name"
            )
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun FieldMappingSection(
    modelFields: List<String>,
    sourceFields: List<String>,
    fieldMapping: Map<String, String>,
    onFieldMappingChange: (String, String) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimension.small)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimension.tiny),
            modifier = Modifier.fillMaxWidth()
        ) {
            val style =
                TextStyle(fontSize = 18.sp, fontFamily = fontOutfit, fontWeight = FontWeight.Medium)

            Text(text = "FIELD", style = style, modifier = Modifier.weight(3f))
            Spacer(modifier = Modifier.weight(1f))
            Text(text = "SOURCE", style = style, modifier = Modifier.weight(3f))
        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(Dimension.extraSmall)
        ) {
            modelFields.forEach { modelField ->
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = modelField,
                            fontSize = 16.sp,
                            fontFamily = fontOutfit,
                            modifier = Modifier.weight(2f)
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_narrow_right_outline),
                            contentDescription = "right arrow",
                            modifier = Modifier.weight(1f)
                        )
                        AutoCompleteTextField(
                            value = fieldMapping[modelField] ?: "",
                            onItemSelected = { onFieldMappingChange(modelField, it) },
                            items = sourceFields,
                            shape = Rounded.large,
                            modifier = Modifier.weight(3f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnSaveAlertDialog(
    schemaName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val textStyle = TextStyle(
        fontFamily = fontOutfit,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium
    )
    AlertDialog(
        title = {
            Text(text = "Warning", style = MaterialTheme.typography.subtitle1)
        },
        text = {
            Text(
                text = """
                   There's already a schema named "$schemaName", do you want to replace it?
               """.trimIndent(),
                style = MaterialTheme.typography.body1
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "OK", style = textStyle)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(text = "Cancel", style = textStyle)
            }
        },
        onDismissRequest = onDismiss
    )
}

@Preview
@Composable
fun EditScreenPreview() {
    val editSchemaViewModel: EditSchemaViewModel by inject()
    val schemaDao: SchemaDao by inject()
    val scope = rememberCoroutineScope()
    SideEffect {
        scope.launch {
            schemaDao.getAll().collect { Log.d(TAG, "EditScreenPreview: $it") }
        }
    }
    AnkiEstrellaTheme {
        EditScreen(editSchemaViewModel)
    }
}
