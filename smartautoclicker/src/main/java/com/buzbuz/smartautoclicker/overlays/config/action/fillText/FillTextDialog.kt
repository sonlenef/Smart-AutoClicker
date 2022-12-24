package com.buzbuz.smartautoclicker.overlays.config.action.fillText

import android.content.Context
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.buzbuz.smartautoclicker.R
import com.buzbuz.smartautoclicker.baseui.DurationInputFilter
import com.buzbuz.smartautoclicker.baseui.dialog.OverlayDialogController
import com.buzbuz.smartautoclicker.databinding.DialogConfigActionFillTextBinding
import com.buzbuz.smartautoclicker.domain.Action
import com.buzbuz.smartautoclicker.overlays.base.bindings.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class FillTextDialog(
    context: Context,
    private val fillText: Action.FillText,
    private val onDeleteFillText: (Action.FillText) -> Unit,
    private val onConfirmFillText: (Action.FillText) -> Unit
) : OverlayDialogController(context, R.style.AppTheme) {

    /** The view model for this dialog. */
    private val viewModel: FillTextViewModel by lazy {
        ViewModelProvider(this)[FillTextViewModel::class.java]
    }

    /** ViewBinding containing the views for this dialog. */
    private lateinit var viewBinding: DialogConfigActionFillTextBinding

    override fun onCreateView(): ViewGroup {
        viewModel.setConfiguredFillText(fillText)

        viewBinding = DialogConfigActionFillTextBinding.inflate(LayoutInflater.from(context)).apply {
            layoutTopBar.apply {
                dialogTitle.setText(R.string.dialog_overlay_title_fill_text)

                buttonDismiss.setOnClickListener { destroy() }
                buttonSave.apply {
                    visibility = View.VISIBLE
                    setOnClickListener { onSaveButtonClicked() }
                }
                buttonDelete.apply {
                    visibility = View.VISIBLE
                    setOnClickListener { onDeleteButtonClicked() }
                }
            }

            editNameLayout.apply {
                setLabel(R.string.input_field_label_name)
                setOnTextChangedListener { viewModel.setName(it.toString()) }
                textField.filters = arrayOf<InputFilter>(
                    InputFilter.LengthFilter(context.resources.getInteger(R.integer.name_max_length))
                )
            }

            editTextLayout.apply {
                setLabel(R.string.input_field_label_text)
                setOnTextChangedListener { viewModel.setText(it.toString()) }
            }

            editPauseDurationLayout.apply {
                textField.filters = arrayOf(DurationInputFilter())
                setLabel(R.string.input_field_label_pause_duration)
                setOnTextChangedListener {
                    viewModel.setPauseDuration(if (it.isNotEmpty()) it.toString().toLong() else null)
                }
            }
        }

        return viewBinding.root
    }

    override fun onDialogCreated(dialog: BottomSheetDialog) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.name.collect(::updateFillTextName) }
                launch { viewModel.nameError.collect(viewBinding.editNameLayout::setError) }
                launch { viewModel.pauseDuration.collect(::updateFillTextDuration) }
                launch { viewModel.pauseDurationError.collect(viewBinding.editPauseDurationLayout::setError) }
                launch { viewModel.isValidAction.collect(::updateSaveButton) }
            }
        }
    }

    private fun onSaveButtonClicked() {
        viewModel.saveLastConfig()
        onConfirmFillText(viewModel.getConfiguredFillText())
        destroy()
    }

    private fun onDeleteButtonClicked() {
        onDeleteFillText(fillText)
        destroy()
    }

    private fun updateFillTextName(newName: String?) {
        viewBinding.editNameLayout.setText(newName)
    }

    private fun updateFillTextDuration(newDuration: String?) {
        viewBinding.editPauseDurationLayout.setText(newDuration)
    }

    private fun updateSaveButton(isValidCondition: Boolean) {
        viewBinding.layoutTopBar.setButtonEnabledState(DialogNavigationButton.SAVE, isValidCondition)
    }
}