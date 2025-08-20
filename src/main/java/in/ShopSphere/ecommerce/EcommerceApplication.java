package in.ShopSphere.ecommerce;

import com.corundumstudio.socketio.SocketIOServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class EcommerceApplication implements CommandLineRunner {

	private final SocketIOServer socketIOServer;

	public static void main(String[] args) {
		SpringApplication.run(EcommerceApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		socketIOServer.start();
		log.info("Socket.IO server started on port: {}", socketIOServer.getConfiguration().getPort());
	}
}
