package com.kronos.data.di

import com.kronos.data.pdf.PdfScanner
import com.kronos.domain.source.PdfDocumentScanner
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PdfModule {

    @Binds
    @Singleton
    abstract fun bindPdfDocumentScanner(impl: PdfScanner): PdfDocumentScanner
}
