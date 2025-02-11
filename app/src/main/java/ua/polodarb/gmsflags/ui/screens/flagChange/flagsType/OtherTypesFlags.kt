package ua.polodarb.gmsflags.ui.screens.flagChange.flagsType

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.dp
import my.nanihadesuka.compose.LazyColumnScrollbar
import ua.polodarb.gmsflags.utils.Extensions.toSortMap
import ua.polodarb.gmsflags.data.databases.local.enities.SavedFlags
import ua.polodarb.gmsflags.ui.components.inserts.ErrorLoadScreen
import ua.polodarb.gmsflags.ui.components.inserts.LoadingProgressBar
import ua.polodarb.gmsflags.ui.components.inserts.NotFoundContent
import ua.polodarb.gmsflags.ui.screens.UiStates
import ua.polodarb.gmsflags.ui.screens.flagChange.FlagChangeScreenViewModel
import ua.polodarb.gmsflags.ui.screens.flagChange.FlagChangeUiStates
import ua.polodarb.gmsflags.ui.screens.flagChange.IntFloatStringValItem
import ua.polodarb.gmsflags.ui.screens.flagChange.SelectFlagsType
import ua.polodarb.gmsflags.ui.screens.flagChange.dialogs.FlagChangeDialog

@Composable
fun OtherTypesFlagsScreen(
    uiState: FlagChangeUiStates,
    viewModel: FlagChangeScreenViewModel,
    packageName: String?,
    flagName: String,
    flagValue: String,
    flagsType: SelectFlagsType,
    editTextValue: String,
    showDialog: Boolean,
    savedFlagsList: List<SavedFlags>,
    onFlagClick: (flagName: String, flagValue: String, editTextValue: String, showDialog: Boolean) -> Unit,
    dialogOnQueryChange: (String) -> Unit,
    dialogOnConfirm: () -> Unit,
    dialogOnDismiss: () -> Unit,
    dialogOnDefault: () -> Unit,
    haptic: HapticFeedback,
    context: Context
) {
    val lazyListState = rememberLazyListState()

    when (uiState) {
        is UiStates.Success -> {

            val textFlagType = when (flagsType) {
                SelectFlagsType.BOOLEAN -> "Boolean"
                SelectFlagsType.INTEGER -> "Integer"
                SelectFlagsType.FLOAT -> "Float"
                SelectFlagsType.STRING -> "String"
            }

            fun setViewModelMethods() = when (flagsType) {

                SelectFlagsType.BOOLEAN -> {}

                SelectFlagsType.INTEGER -> {
                    viewModel.updateIntFlagValue(
                        flagName,
                        editTextValue
                    )
                    viewModel.overrideFlag(
                        packageName = packageName.toString(),
                        name = flagName,
                        intVal = editTextValue
                    )
                    viewModel.initIntValues()
                    viewModel.initOverriddenIntFlags(packageName.toString())
                }

                SelectFlagsType.FLOAT -> {
                    viewModel.updateFloatFlagValue(
                        flagName,
                        editTextValue
                    )
                    viewModel.overrideFlag(
                        packageName = packageName.toString(),
                        name = flagName,
                        floatVal = editTextValue
                    )
                    viewModel.initFloatValues()
                    viewModel.initOverriddenFloatFlags(packageName.toString())
                }

                SelectFlagsType.STRING -> {
                    viewModel.updateStringFlagValue(
                        flagName,
                        editTextValue
                    )
                    viewModel.overrideFlag(
                        packageName = packageName.toString(),
                        name = flagName,
                        stringVal = editTextValue
                    )
                    viewModel.initStringValues()
                    viewModel.initOverriddenStringFlags(packageName.toString())
                }
            }

            val listInt = uiState.data.toList().sortedBy { it.first }.toMap().toSortMap()

            if (listInt.isEmpty()) NotFoundContent()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
            ) {
                if (listInt.isNotEmpty()) {
                    LazyColumnScrollbar(
                        listState = lazyListState,
                        thickness = 8.dp,
                        padding = 0.dp,
                        thumbColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                        thumbSelectedColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                        thumbMinHeight = 0.075f,
                        enabled = listInt.size >= 15
                    ) {
                        LazyColumn(
                            state = lazyListState
                        ) {
                            itemsIndexed(listInt.toList()) { index, item ->

                                val targetFlag = SavedFlags(
                                    packageName.toString(),
                                    item.first,
                                    flagsType.name
                                )
                                val isEqual =
                                    savedFlagsList.any { (packageName, flag, selectFlagsType, _) ->
                                        packageName == targetFlag.pkgName &&
                                                flag == targetFlag.flagName &&
                                                selectFlagsType == targetFlag.type
                                    }

                                IntFloatStringValItem(
                                    flagName = listInt.keys.toList()[index],
                                    flagValue = listInt.values.toList()[index],
                                    lastItem = index == listInt.size - 1,
                                    saveChecked = isEqual,
                                    saveOnCheckedChange = {
                                        if (it) {
                                            viewModel.saveFlag(
                                                item.first,
                                                packageName.toString(),
                                                flagsType.name
                                            )
                                        } else {
                                            viewModel.deleteSavedFlag(
                                                item.first,
                                                packageName.toString()
                                            )
                                        }
                                    },
                                    onClick = {
                                        onFlagClick(
                                            item.first,
                                            item.second,
                                            flagValue,
                                            showDialog
                                        )
                                    },
                                    onLongClick = { }
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.padding(12.dp))
                            }
                        }
                    }
                    FlagChangeDialog(
                        showDialog = showDialog,
                        flagName = flagName,
                        flagValue = flagValue,
                        onQueryChange = {
                            dialogOnQueryChange(it)
                        },
                        flagType = textFlagType,
                        onConfirm = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            setViewModelMethods()
//                            viewModel.initAllFlags()
//                            viewModel.initAllOverriddenFlagsByPackage(packageName.toString())
                            dialogOnConfirm()
                        },
                        onDismiss = dialogOnDismiss,
                        onDefault = dialogOnDefault
                    )
                } else {
                    LoadingProgressBar()
                }
            }
        }

        is UiStates.Loading -> {
            LoadingProgressBar()
        }

        is UiStates.Error -> {
            ErrorLoadScreen()
        }
    }
}