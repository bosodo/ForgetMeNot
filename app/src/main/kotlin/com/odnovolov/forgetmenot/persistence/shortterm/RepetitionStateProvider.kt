package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.interactor.repetition.Repetition
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionCard
import com.odnovolov.forgetmenot.persistence.shortterm.RepetitionStateProvider.SerializableState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class RepetitionStateProvider(
    json: Json,
    database: Database,
    private val globalState: GlobalState,
    override val key: String = Repetition.State::class.qualifiedName!!
) : BaseSerializableStateProvider<Repetition.State, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val serializableRepetitionCards: List<SerializableRepetitionCard>,
        val repetitionCardPosition: Int,
        val speakEventPosition: Int,
        val isPlaying: Boolean,
        val numberOfLaps: Int,
        val currentLap: Int
    )

    @Serializable
    data class SerializableRepetitionCard(
        val id: Long,
        val cardId: Long,
        val isAnswered: Boolean,
        val isReverse: Boolean,
        val pronunciationId: Long,
        val speakPlanId: Long
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: Repetition.State): SerializableState {
        val serializableRepetitionCards: List<SerializableRepetitionCard> = state.repetitionCards
            .map { repetitionCard: RepetitionCard ->
                with(repetitionCard) {
                    SerializableRepetitionCard(
                        id = id,
                        cardId = card.id,
                        isAnswered = isAnswered,
                        isReverse = isReverse,
                        pronunciationId = pronunciation.id,
                        speakPlanId = speakPlan.id
                    )
                }
            }
        return SerializableState(
            serializableRepetitionCards,
            state.repetitionCardPosition,
            state.speakEventPosition,
            state.isPlaying,
            state.numberOfLaps,
            state.currentLap
        )
    }

    override fun toOriginal(serializableState: SerializableState): Repetition.State {
        val cardMap: Map<Long, Card> = globalState.decks
            .flatMap { deck -> deck.cards }
            .associateBy { card -> card.id }
        val pronunciationMap: Map<Long, Pronunciation> =
            globalState.sharedPronunciations
                .plus(globalState.decks.map { deck: Deck -> deck.exercisePreference.pronunciation })
                .associateBy { pronunciation: Pronunciation -> pronunciation.id }
        val repetitionCards: List<RepetitionCard> = serializableState.serializableRepetitionCards
            .map { serializableRepetitionCard: SerializableRepetitionCard ->
                with(serializableRepetitionCard) {
                    RepetitionCard(
                        id = id,
                        card = cardMap.getValue(cardId),
                        isAnswered = isAnswered,
                        isReverse = isReverse,
                        pronunciation = pronunciationMap.getValue(pronunciationId),
                        speakPlan = SpeakPlan.Default // todo
                    )
                }
            }
        return Repetition.State(
            repetitionCards,
            serializableState.repetitionCardPosition,
            serializableState.speakEventPosition,
            serializableState.isPlaying,
            serializableState.numberOfLaps,
            serializableState.currentLap
        )
    }
}