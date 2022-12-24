package com.buzbuz.smartautoclicker.overlays.config.action.fillText

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import com.buzbuz.smartautoclicker.domain.Action
import com.buzbuz.smartautoclicker.overlays.base.utils.getEventConfigPreferences
import com.buzbuz.smartautoclicker.overlays.base.utils.putClickPressDurationConfig
import kotlinx.coroutines.flow.*

class FillTextViewModel(application: Application) : AndroidViewModel(application) {
    /** The action being configured by the user. Defined using [configuredFillText]. */
    private val configuredFillText = MutableStateFlow<Action.FillText?>(null)

    /** Event configuration shared preferences. */
    private val sharedPreferences: SharedPreferences = application.getEventConfigPreferences()

    /** The name of the click. */
    val name: Flow<String?> = configuredFillText
        .filterNotNull()
        .map { it.name }
        .take(1)

    /** Tells if the action name is valid or not. */
    val nameError: Flow<Boolean> = configuredFillText.map { it?.name?.isEmpty() ?: true }

    /** The duration between the press and release of the click in milliseconds. */
    val pauseDuration: Flow<String?> = configuredFillText
        .filterNotNull()
        .map { it.pauseDuration?.toString() }
        .take(1)

    /** Tells if the press duration value is valid or not. */
    val pauseDurationError: Flow<Boolean> = configuredFillText.map { (it?.pauseDuration ?: -1) <= 0 }


    /** Tells if the configured click is valid and can be saved. */
    val isValidAction: Flow<Boolean> = configuredFillText
        .map { fillText ->
            fillText != null && !fillText.name.isNullOrEmpty()
                    && fillText.text.isNotEmpty() && fillText.pauseDuration != null
        }

    /**
     * Set the configured click.
     * This will update all values represented by this view model.
     *
     * @param click the click to configure.
     */
    fun setConfiguredFillText(fillText: Action.FillText) {
        configuredFillText.value = fillText.deepCopy()
    }

    /** @return the click containing all user changes. */
    fun getConfiguredFillText(): Action.FillText =
        configuredFillText.value
            ?: throw IllegalStateException("Can't get the configured fill text, none were defined.")

    /**
     * Set the name of the click.
     * @param name the new name.
     */
    fun setName(name: String) {
        configuredFillText.value?.let { click ->
            configuredFillText.value = click.copy(name = "" + name)
        }
    }

    /**
     * Set the name of the click.
     * @param text the fill text.
     */
    fun setText(text: String) {
        configuredFillText.value?.let { fillText ->
            configuredFillText.value = fillText.copy(text = "" + text)
        }
    }

    /**
     * Set the press duration of the click.
     * @param durationMs the new duration in milliseconds.
     */
    fun setPauseDuration(durationMs: Long?) {
        configuredFillText.value?.let { fillText ->
            configuredFillText.value = fillText.copy(pauseDuration = durationMs)
        }
    }

    /** Save the configured values to restore them at next creation. */
    fun saveLastConfig() {
        configuredFillText.value?.let { fillText ->
            sharedPreferences.edit().putClickPressDurationConfig(fillText.pauseDuration ?: 0).apply()
        }
    }
}