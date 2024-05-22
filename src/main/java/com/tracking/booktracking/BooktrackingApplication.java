package com.tracking.booktracking;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.fasterxml.jackson.databind.util.JSONWrappedObject;
import com.tracking.booktracking.connect.DataStaxAstraProperties;
import com.tracking.booktracking.model.Author;
import com.tracking.booktracking.repo.AuthorRepository;
import jakarta.annotation.PostConstruct;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@SpringBootApplication
@EnableConfigurationProperties(DataStaxAstraProperties.class)
public class BooktrackingApplication {

	@Autowired AuthorRepository authorRepository;

//	@Value("${dump.location.author}")
//	private String authorDumpLocation;
//
//	@Value("${dump.location.works}")
//	private String worksDumpLocation;

	public static void main(String[] args) {
		SpringApplication.run(BooktrackingApplication.class, args);
	}

	@Bean
	public CqlSessionBuilderCustomizer sessionBuilderCustomizer(DataStaxAstraProperties astraProperties){
		Path bundle = astraProperties.getSecureConnectBundle().toPath();
		return builder->builder.withCloudSecureConnectBundle(bundle);
	}

	@PostConstruct
	public void start(){
		System.out.println("Application Started");
		initAuthors();

	}

	private void initAuthors() {
		String authorDumpLocation = "C:/Users/arora/Downloads/test-authors.txt";
		System.out.println(authorDumpLocation);
		Path path = Paths.get(authorDumpLocation);
		try(Stream<String> lines = Files.lines(path)){
			lines.forEach( line ->
					{
							String jsonString = line.substring(line.indexOf('{'));
							JSONObject jsonObject = new JSONObject(jsonString);
							Author author = new Author();
							author.setName(jsonObject.optString("name"));
							author.setPersonalName(jsonObject.optString("personal_name"));
							author.setId(jsonObject.optString("key").replace("/authors/", ""));
							authorRepository.save(author);
							System.out.println("Author Saved : " + author.getName());

					}
			);
		}
		catch ( Exception e){
			e.printStackTrace();
		}
	}

}
