package com.cardstack.app.di

import android.content.Context
import androidx.room.Room
import com.cardstack.app.data.db.AppDatabase
import com.cardstack.app.data.db.CardDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "cardstack.db")
            .build()

    @Provides
    fun provideCardDao(db: AppDatabase): CardDao = db.cardDao()
}
