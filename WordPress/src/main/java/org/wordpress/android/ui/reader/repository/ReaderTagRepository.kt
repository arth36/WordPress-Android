package org.wordpress.android.ui.reader.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.wordpress.android.models.ReaderTag
import org.wordpress.android.models.ReaderTagList
import org.wordpress.android.models.ReaderTagType
import org.wordpress.android.modules.BG_THREAD
import org.wordpress.android.modules.IO_THREAD
import org.wordpress.android.ui.reader.repository.usecases.tags.FetchInterestTagsUseCase
import org.wordpress.android.ui.reader.repository.usecases.tags.FollowInterestTagsUseCase
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.SECONDS
import javax.inject.Inject
import javax.inject.Named

/**
 * ReaderTagRepository is middleware that encapsulates data related business related data logic
 * Handle communicate with ReaderServices and Actions
 */
class ReaderTagRepository @Inject constructor(
    @Named(BG_THREAD) private val bgDispatcher: CoroutineDispatcher,
    @Named(IO_THREAD) private val ioDispatcher: CoroutineDispatcher,
    private val fetchInterestTagUseCase: FetchInterestTagsUseCase,
    private val followInterestTagsUseCase: FollowInterestTagsUseCase
) {
    private val mutableRecommendedInterests = MutableLiveData<ReaderTagList>()
    private val recommendedInterests: LiveData<ReaderTagList> = mutableRecommendedInterests

    suspend fun getInterests(): ReaderRepositoryCommunication {
        return withContext(ioDispatcher) {
            fetchInterestTagUseCase.fetch()
        }
    }

    suspend fun getUserTags(isEmpty: Boolean = true): ReaderTagList =
            withContext(ioDispatcher) {
                delay(SECONDS.toMillis(5))
                getMockUserTags(isEmpty)
            }

    suspend fun saveInterests(tags: List<ReaderTag>): ReaderRepositoryCommunication {
        return withContext(ioDispatcher) {
            followInterestTagsUseCase.followInterestTags(tags)
        }
    }

    suspend fun getRecommendedInterests(): LiveData<ReaderTagList> =
            withContext(bgDispatcher) {
                delay(TimeUnit.SECONDS.toMillis(5))
                getMockRecommendedInterests()
            }

    private suspend fun getMockRecommendedInterests(): LiveData<ReaderTagList> {
        return withContext(ioDispatcher) {
            mutableRecommendedInterests.postValue(getMockInterests())
            recommendedInterests
        }
    }

    private fun getMockUserTags(isEmpty: Boolean): ReaderTagList {
        return if (isEmpty) {
            ReaderTagList()
        } else {
            getMockInterests()
        }
    }

    // todo: remove method post implementation
    private fun getMockInterests() =
            ReaderTagList().apply {
                for (c in 'A'..'Z')
                    (add(
                            ReaderTag(
                                    c.toString(), c.toString(), c.toString(),
                                    "https://public-api.wordpress.com/rest/v1.2/read/tags/$c/posts",
                                    ReaderTagType.DEFAULT
                            )
                    ))
            }
}
