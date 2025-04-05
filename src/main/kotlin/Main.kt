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

class GitHubRepository(private val apiService: GitHubApiService) {
    private val userCache = mutableMapOf<String, GitHubUser>()

    fun getUserInfo(username: String): GitHubUser? {
        if (userCache.containsKey(username)) {
            println("Loaded from cache.")
            return userCache[username]
        }

        // Basic API implementation (without error handling)
        val userResponse = apiService.getUser(username).execute()
        val userData = userResponse.body()!!

        val reposResponse = apiService.getUserRepos(username).execute()
        val reposData = reposResponse.body()!!

        val gitHubUser = GitHubUser(
            login = userData.login,
            followers = userData.followers,
            following = userData.following,
            createdAt = userData.createdAt,
            repos = reposData
        )
        userCache[username] = gitHubUser
        return gitHubUser
    }

    fun getCachedUsers(): List<GitHubUser> = userCache.values.toList()
}