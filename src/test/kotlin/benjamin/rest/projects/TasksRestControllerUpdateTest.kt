package benjamin.rest.projects

import benjamin.projects.tasks.api.TaskStatus
import benjamin.projects.tasks.api.UpdateTaskCommand
import benjamin.projects.tasks.api.UpdateTaskResult
import benjamin.rest.models.ProjectModel
import benjamin.security.Oauth2SecurityConfig
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.web.servlet.MockHttpServletRequestDsl
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.put

@WebMvcTest(controllers = [TasksRestController::class])
@Import(Oauth2SecurityConfig::class)
class TasksRestControllerUpdateTest {
    @Autowired
    private lateinit var web: MockMvc

    @MockBean
    private lateinit var projectModel: ProjectModel

    private val updateTaskCommandJson = """
        {
          "description": "Update dependencies",
          "assignee": "adamelmurzaev95",
          "status": "DONE"
        }
    """.trimIndent()

    private val updateTaskCommand = UpdateTaskCommand(
        description = "Update dependencies",
        assignee = "adamelmurzaev95",
        status = TaskStatus.DONE
    )

    @Test
    fun `should return 401 Unauthorized when no jwt token is provided`() {
        web.put("/projects/Google/tasks/1")
            .andExpect {
                status {
                    isUnauthorized()
                }
            }
    }

    @Test
    fun `should return 400 Bad Request when invalid body provided`() {
        web.put("/projects/Google/tasks/1") {
            mockJwt()
            contentType = MediaType.APPLICATION_JSON
            content = "{"
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `should return 404 Not Found when assignee with such username doesnt exist`() {
        Mockito.`when`(projectModel.updateTask(1, updateTaskCommand))
            .thenReturn(UpdateTaskResult.AssigneeNotFound)

        web.put("/projects/Google/tasks/1") {
            mockJwt()
            contentType = MediaType.APPLICATION_JSON
            content = updateTaskCommandJson
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `should return 404 Not Found when task with such id doesnt exist`() {
        Mockito.`when`(projectModel.updateTask(1, updateTaskCommand))
            .thenReturn(UpdateTaskResult.TaskNotFound)

        web.put("/projects/Google/tasks/1") {
            mockJwt()
            contentType = MediaType.APPLICATION_JSON
            content = updateTaskCommandJson
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `should return 200 OK when there no problems`() {
        Mockito.`when`(projectModel.updateTask(1, updateTaskCommand))
            .thenReturn(UpdateTaskResult.Success)

        web.put("/projects/Google/tasks/1") {
            mockJwt()
            contentType = MediaType.APPLICATION_JSON
            content = updateTaskCommandJson
        }.andExpect {
            status { isOk() }
        }
    }

    private fun MockHttpServletRequestDsl.mockJwt() =
        with(SecurityMockMvcRequestPostProcessors.jwt().jwt { it.claim("user_name", "adamelmurzaev95") })
}