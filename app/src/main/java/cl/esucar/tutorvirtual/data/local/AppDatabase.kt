package cl.esucar.tutorvirtual.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UserEntity::class, StageEntity::class, StepEntity::class,
        FaqEntity::class, ProgressEntity::class, ReminderEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun contentDao(): ContentDao
    abstract fun progressDao(): ProgressDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        /** v1 -> v2: perfil profesor (rol de usuario) y observaciones/revisión del profesor en el avance. */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN role TEXT NOT NULL DEFAULT 'ESTUDIANTE'")
                db.execSQL("ALTER TABLE progress ADD COLUMN feedback TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE progress ADD COLUMN reviewedAt INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext, AppDatabase::class.java, "tutor_virtual.db"
            ).addMigrations(MIGRATION_1_2).build().also { instance = it }
        }
    }
}
