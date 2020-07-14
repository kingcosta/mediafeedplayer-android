package com.jppappstudio.mediafeedplayer.android.models

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope

@Database(entities = [Channel::class, Listing::class], version = 2, exportSchema = false)
public abstract class AppRoomDatabase : RoomDatabase() {

    abstract fun ChannelDao(): ChannelDao
    abstract fun FavouritesDao(): FavouritesDao

    companion object {
        @Volatile
        private var INSTANCE: AppRoomDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                val MIGRATION_1_2 = object: Migration(1, 2) {
                    override fun migrate(database: SupportSQLiteDatabase) {
                        database.execSQL("CREATE TABLE `favourites` (`id` INTEGER, `title` STRING, `url` STRING, `thumbnailURL` STRING, `description` STRING, `type` STRING, `bookmarkable` INTEGER, PRIMARY KEY(`id`))")
                    }
                }

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppRoomDatabase::class.java,
                    "mfd.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}