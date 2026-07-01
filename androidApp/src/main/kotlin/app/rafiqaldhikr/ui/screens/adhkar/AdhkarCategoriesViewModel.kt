package app.rafiqaldhikr.ui.screens.adhkar

import androidx.lifecycle.ViewModel
import app.rafiq.domain.repository.AdhkarRepository

class AdhkarCategoriesViewModel(
    private val repository: AdhkarRepository
) : ViewModel() {

    val categories = listOf(
        "morning"  to "أذكار الصباح",
        "evening"  to "أذكار المساء",
        "sleep"    to "أذكار النوم",
        "prayer"   to "أذكار الصلاة",
        "misc"     to "أذكار متنوعة"
    )
}
