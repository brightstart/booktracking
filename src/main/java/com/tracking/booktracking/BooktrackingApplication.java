package com.tracking.booktracking;

import com.tracking.booktracking.connect.DataStaxAstraProperties;
import com.tracking.booktracking.model.Author;
import com.tracking.booktracking.model.Book;
import com.tracking.booktracking.repo.AuthorRepository;
import jakarta.annotation.PostConstruct;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootApplication
@EnableConfigurationProperties(DataStaxAstraProperties.class)
public class BooktrackingApplication {

	@Autowired AuthorRepository authorRepository;

	@Value("${datadump.location.author}")
	private String authorDumpLocation;

	@Value("${datadump.location.works}")
	private String worksDumpLocation;

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
		initWorks();

	}

	private void initAuthors() {
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
	private void initWorks() {
		System.out.println(authorDumpLocation);
		Path path = Paths.get(authorDumpLocation);
		try(Stream<String> lines = Files.lines(path)){
			lines.forEach( line ->
					{
						String jsonString = line.substring(line.indexOf('{'));
						try {
							JSONObject worksJsonObject = new JSONObject(jsonString);
							Book book = new Book();
							book.setId(worksJsonObject.optString("key").replace("/works/", ""));
							book.setName(worksJsonObject.optString("title"));

							JSONObject descriptionObj = worksJsonObject.optJSONObject("description");
							if (descriptionObj != null)
								book.setBookDesc(descriptionObj.optString("value"));

							JSONObject publishedDateObj = worksJsonObject.optJSONObject("created");
							if (publishedDateObj != null)
								book.setPublishedDate(LocalDate.parse(publishedDateObj.optString("value")));

							JSONArray coversJsonArray = worksJsonObject.optJSONArray("covers");
							if (coversJsonArray != null) {
								List<String> covers = new ArrayList<>();
								for (int i = 0; i < coversJsonArray.length(); i++)
									covers.add(coversJsonArray.getString(i));
								book.setCoverIds(covers);
							}

							JSONArray authorsJsonArray = worksJsonObject.optJSONArray("authors");
							if (coversJsonArray != null) {
								List<String> authorIds = new ArrayList<>();
								for (int i = 0; i < authorsJsonArray.length(); i++) {
									String authorId = authorsJsonArray.getJSONObject(i)
											.getJSONObject("author")
											.getString("key")
											.replace("/authors/", "");
									authorIds.add(authorId);
								}
								book.setAuthorIds(authorIds);
								List<String> authorNames = authorIds.stream().map(id -> authorRepository.findById(id))
										.map(optionalAuthor -> {
											if (optionalAuthor.isEmpty()) return "Unknown Author";
											return optionalAuthor.get().getName();
										}).collect(Collectors.toList());

								book.setAuthorNames(authorNames);
							}

						}
						catch (Exception e){
							e.printStackTrace();
						}
					}
			);
		}
		catch ( Exception e){
			e.printStackTrace();
		}
	}

}
