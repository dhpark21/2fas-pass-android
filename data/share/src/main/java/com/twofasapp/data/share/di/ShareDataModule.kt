package com.twofasapp.data.share.di

import com.twofasapp.core.di.KoinModule
import com.twofasapp.data.share.ShareRepository
import com.twofasapp.data.share.ShareRepositoryImpl
import com.twofasapp.data.share.mapper.ShareMapper
import com.twofasapp.data.share.remote.ShareRemoteSource
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

class ShareDataModule : KoinModule {
    override fun provide(): Module = module {
        singleOf(::ShareMapper)
        singleOf(::ShareRemoteSource)
        singleOf(::ShareRepositoryImpl) { bind<ShareRepository>() }
    }
}