package `in`.francl.cam.domain.port.outbound.authorization

/**
 * Foco: A interface se concentra na comunicação com um sistema externo (provavelmente um serviço de autorização) para obter tokens.
 * Abstração: Ela abstrai a implementação específica do serviço de autorização e o protocolo utilizado para a comunicação.
 * @author Franclin Sousa
 */
interface AuthorizationGateway {
    suspend fun retrieve(scopes: Set<String>,): Result<Tokenable>
}
