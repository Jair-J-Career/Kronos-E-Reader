package com.kronos.data.di

import com.kronos.data.pdf.TextExtractionSourceImpl
import com.kronos.domain.source.TextExtractionSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TextExtractionModule {

    @Binds
    @Singleton
    abstract fun bindTextExtractionSource(impl: TextExtractionSourceImpl): TextExtractionSource
}
