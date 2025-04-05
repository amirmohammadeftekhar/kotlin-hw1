import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.Call


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

// Combined user data with their public repositories
data class GitHubUser(
    val login: String,
    val followers: Int,
    val following: Int,
    val createdAt: String,
    val repos: List<Repository>
)

// Retrofit API interface for GitHub
interface GitHubApiService {
    @GET("users/{username}")
    fun getUser(@Path("username") username: String): Call<UserResponse>

    @GET("users/{username}/repos")
    fun getUserRepos(@Path("username") username: String): Call<List<Repository>>
}