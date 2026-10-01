package com.oraclejava.demodog.frontend;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Spring 기동이 끝나면 frontend 폴더에 배포된 Next.js standalone 서버(node server.js)를
 * 자식 프로세스로 띄우고, Spring이 종료될 때 함께 종료한다.
 * Next.js의 route handler는 SPRING_API_URL 환경변수로 이 Spring 서버를 호출한다.
 */
@Component
@ConditionalOnProperty(name = "demodog.frontend.enabled", havingValue = "true", matchIfMissing = true)
public class NextJsServer implements DisposableBean {

	private static final Logger log = LoggerFactory.getLogger(NextJsServer.class);

	private final Path directory;

	private final int port;

	private final String nodeCommand;

	private final Environment environment;

	private Process process;

	public NextJsServer(@Value("${demodog.frontend.dir:frontend}") String directory,
			@Value("${demodog.frontend.port:3000}") int port,
			@Value("${demodog.frontend.node:node}") String nodeCommand, Environment environment) {
		this.directory = Paths.get(directory).toAbsolutePath().normalize();
		this.port = port;
		this.nodeCommand = nodeCommand;
		this.environment = environment;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void start() throws IOException {
		Path serverJs = this.directory.resolve("server.js");
		if (!Files.isRegularFile(serverJs)) {
			log.warn("Next.js 서버를 찾을 수 없어 Spring만 실행합니다: {} (./gradlew deployFrontend 로 배포)", serverJs);
			return;
		}

		String springPort = this.environment.getProperty("local.server.port", "8080");
		ProcessBuilder builder = new ProcessBuilder(this.nodeCommand, "server.js").directory(this.directory.toFile())
			.inheritIO();
		Map<String, String> env = builder.environment();
		env.put("PORT", String.valueOf(this.port));
		env.put("HOSTNAME", "0.0.0.0");
		env.put("NODE_ENV", "production");
		env.put("SPRING_API_URL", "http://localhost:" + springPort);

		this.process = builder.start();
		log.info("Next.js 서버 시작: http://localhost:{} (pid {}, SPRING_API_URL=http://localhost:{})", this.port,
				this.process.pid(), springPort);
	}

	@Override
	public void destroy() {
		if (this.process == null || !this.process.isAlive()) {
			return;
		}
		log.info("Next.js 서버 종료 (pid {})", this.process.pid());
		this.process.descendants().forEach(ProcessHandle::destroy);
		this.process.destroy();
	}

}
