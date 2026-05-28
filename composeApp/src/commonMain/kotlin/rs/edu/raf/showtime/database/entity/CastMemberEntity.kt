package rs.edu.raf.showtime.database.entity

import androidx.room.Entity

@Entity(
    tableName = "cast_members",
    primaryKeys = ["movieId", "personId"]
)
data class CastMemberEntity(
    val movieId: String,
    val personId: String,
    val name: String,
    val profilePath: String? = null,
)
