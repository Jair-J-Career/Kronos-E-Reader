package com.kronos.data.di

import com.kronos.data.pdf.CoverGenerator
import com.kronos.domain.source.CoverExtractor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CoverModule {

    @Binds
    @Singleton
    abstract fun bindCoverExtractor(impl: CoverGenerator): CoverExtractor
}
