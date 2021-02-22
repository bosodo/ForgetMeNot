package com.odnovolov.forgetmenot.presentation.screen.home

import com.odnovolov.forgetmenot.domain.interactor.searcher.SearchCard
import java.io.OutputStream

sealed class HomeEvent {
    class SearchTextChanged(val searchText: String) : HomeEvent()
    class GotFilesCreationResult(val filesCreationResult: List<FileCreationResult>) : HomeEvent() {
        data class FileCreationResult(val fileName: String, val outputStream: OutputStream?)
    }
    object DecksRemovedSnackbarCancelButtonClicked : HomeEvent()

    // Selection toolbar:
    object SelectionCancelled : HomeEvent()
    object ExportButtonClicked : HomeEvent()
    object SelectAllDecksButtonClicked : HomeEvent()
    object RemoveDecksButtonClicked : HomeEvent()

    // Filters:
    object DecksAvailableForExerciseCheckboxClicked : HomeEvent()

    // Sorting:
    object SortingDirectionButtonClicked : HomeEvent()
    class SortByButtonClicked(val criterion: DeckSorting.Criterion) : HomeEvent()

    // Deck item:
    class DeckButtonClicked(val deckId: Long) : HomeEvent()
    class DeckButtonLongClicked(val deckId: Long) : HomeEvent()
    class DeckSelectorClicked(val deckId: Long) : HomeEvent()

    // Deck options:
    class DeckOptionButtonClicked(val deckId: Long) : HomeEvent()
    object StartExerciseDeckOptionSelected : HomeEvent()
    object AutoplayDeckOptionSelected : HomeEvent()
    object RenameDeckOptionSelected : HomeEvent()
    object EditCardsDeckOptionSelected : HomeEvent()
    object SetupDeckOptionSelected : HomeEvent()
    object ExportDeckOptionSelected : HomeEvent()
    object RemoveDeckOptionSelected : HomeEvent()

    // Bottom buttons:
    object AutoplayButtonClicked : HomeEvent()
    object ExerciseButtonClicked : HomeEvent()

    // Card item:
    class FoundCardClicked(val searchCard: SearchCard) : HomeEvent()
}