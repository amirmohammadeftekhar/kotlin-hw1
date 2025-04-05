import com.google.gson.annotations.SerializedName

// API response for user data
data class UserResponse(
    val login: String,
    val followers: Int,
    val following: Int,
    @SerializedName("created_at") val createdAt: String
)

// API response for each repository
data class Repository(
    val name: String
)