import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Scanner



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

        val userResponse = apiService.getUser(username).execute()
        if (!userResponse.isSuccessful) {
            println("Error fetching user info: ${userResponse.code()}")
            return null
        }
        val userData = userResponse.body() ?: run {
            println("No user data found.")
            return null
        }

        val reposResponse = apiService.getUserRepos(username).execute()
        if (!reposResponse.isSuccessful) {
            println("Error fetching repositories: ${reposResponse.code()}")
            return null
        }
        val reposData = reposResponse.body() ?: listOf()

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

    fun searchUserByUsername(query: String): List<GitHubUser> {
        return userCache.values.filter { it.login.contains(query, ignoreCase = true) }
    }

    fun searchRepoByName(query: String): List<Pair<GitHubUser, Repository>> {
        return userCache.values.flatMap { user ->
            user.repos.filter { it.name.contains(query, ignoreCase = true) }
                .map { repo -> user to repo }
        }
    }

    fun getCachedUsers(): List<GitHubUser> = userCache.values.toList()
}

// CLI Main Program
fun main() {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService = retrofit.create(GitHubApiService::class.java)
    val repository = GitHubRepository(apiService)
    val scanner = Scanner(System.`in`)

    while (true) {
        println("""
            ------------------------------
            Menu:
            1.Fetch user info by username
            2.Show cached users
            3.Search user by username in cache
            4.Search repository name in cache
            5.Exit
            ------------------------------
            Enter your choice:
        """.trimIndent())


        when (scanner.nextLine().trim()) {
            "1" -> {
                print("Enter GitHub username: ")
                val username = scanner.nextLine().trim()
                val user = repository.getUserInfo(username)
                if (user != null) {
                    println("\nUsername: ${user.login}")
                    println("Followers: ${user.followers}")
                    println("Following: ${user.following}")
                    println("Created At: ${user.createdAt}")
                    println("Repositories:")
                    if (user.repos.isEmpty()) println("No repositories found.")
                    else user.repos.forEach { println("- ${it.name}") }
                }
            }
            "2" -> {
                val users = repository.getCachedUsers()
                if (users.isEmpty()) {
                    println("No users cached.")
                } else {
                    println("Cached Users:")
                    users.forEach { println("- ${it.login}") }
                }
            }
            "3" -> {
                print("Enter search query: ")
                val query = scanner.nextLine().trim()
                val results = repository.searchUserByUsername(query)
                if (results.isEmpty()) println("No users found.")
                else results.forEach { println("- ${it.login}") }
            }
            "4" -> {
                print("Enter repository name to search: ")
                val query = scanner.nextLine().trim()
                val results = repository.searchRepoByName(query)
                if (results.isEmpty()) println("No matching repositories found.")
                else results.forEach { (user, repo) ->
                    println("User: ${user.login} -> Repo: ${repo.name}")
                }
            }
            "5" -> {
                println("Exiting program. Goodbye!")
                break
            }
            else -> println("Invalid option. Try again.")
        }
    }
}
