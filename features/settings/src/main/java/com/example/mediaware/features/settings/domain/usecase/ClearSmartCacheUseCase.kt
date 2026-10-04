package com.example.mediaware.features.settings.domain.usecase

import com.example.mediaware.core.common.result.Resource
import com.example.mediaware.core.database.dao.MedicineCacheDao
import com.example.mediaware.core.database.dao.TestInfoCacheDao
import javax.inject.Inject

class ClearSmartCacheUseCase @Inject constructor(
    private val medicineCacheDao: MedicineCacheDao,
    private val testInfoCacheDao: TestInfoCacheDao
) {
    suspend operator fun invoke(): Resource<Unit> {
        return try {
            medicineCacheDao.clearAllCache()
            testInfoCacheDao.clearAllCache()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("ক্যাশে পরিষ্কার করতে সমস্যা হয়েছে: ${e.localizedMessage}")
        }
    }
}
