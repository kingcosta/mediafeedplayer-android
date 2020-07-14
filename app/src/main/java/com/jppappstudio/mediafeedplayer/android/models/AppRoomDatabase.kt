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
                        database.execSQL(
                            "CREATE TABLE `favourites` (\n" +
                                    "  `id` INTEGER NOT NULL DEFAULT null, \n" +
                                    "  `title` TEXT NOT NULL DEFAULT null, \n" +
                                    "  `url` TEXT NOT NULL DEFAULT null, \n" +
                                    "  `thumbnailURL` TEXT NOT NULL DEFAULT null, \n" +
                                    "  `description` TEXT NOT NULL DEFAULT null, \n" +
                                    "  `type` TEXT NOT NULL DEFAULT null, \n" +
                                    "  `bookmarkable` INTEGER NOT NULL DEFAULT null, \n" +
                                    "  PRIMARY KEY(`id`)\n" +
                                    ")")
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