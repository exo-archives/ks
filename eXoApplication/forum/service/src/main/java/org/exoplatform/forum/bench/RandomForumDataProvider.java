package org.exoplatform.forum.bench;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.exoplatform.services.log.Log;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.services.idgenerator.IDGeneratorService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.organization.OrganizationService;

public class RandomForumDataProvider implements ForumDataProvider {

	private static Log log = ExoLogger.getLogger(RandomForumDataProvider.class);
	private LoremIpsum4J textGen;
	private Random rand;
	private List<String> users;
	private int maxCategories = 10;
	private int maxForums = 5;
	private int maxTopics = 20;
	private int maxPosts = 20;
	private boolean randomize = false;
	private ForumService forumService;

	public RandomForumDataProvider(InitParams initParams,
			ForumService forumService, IDGeneratorService uidGenerator,
			OrganizationService organizationService) {
		this.forumService = forumService;
		initRandomizers();
		initParams(initParams);

	}

	private void initParams(InitParams initParams) {
		try {
			maxCategories = Integer.parseInt(initParams.getValueParam(
					"maxCategories").getValue());
			maxForums = Integer.parseInt(initParams.getValueParam("maxForums")
					.getValue());
			maxTopics = Integer.parseInt(initParams.getValueParam("maxTopics")
					.getValue());
			maxPosts = Integer.parseInt(initParams.getValueParam("maxPosts")
					.getValue());
			randomize = Boolean.parseBoolean(initParams.getValueParam("randomize")
					.getValue());
			log.debug("initializing : " + initParams);
		} catch (Exception e) {
			throw new RuntimeException(
					"Could not initialize " , e);
		}
	}

	private void initRandomizers() {
		textGen = new LoremIpsum4J();
		rand = new Random();
	}

	private String randomWords(int i) {
		int wordCount = rand.nextInt(i + 1) + 1;
		String words = textGen.getWords(wordCount);
		return words;

	}
	

	private String randomParagraphs(int i) {
		int paragraphCount = rand.nextInt(i + 1) + 1;
		String  paragraphs = textGen.getParagraphs(paragraphCount);
		return paragraphs.replaceAll("\\n\\n","<br/><br/>");
	}	
	

	public List<Category> findCategories() {

		List<Category> result = new ArrayList<Category>();
		try {
			// init marker
			Category init = newCategory(null);
			init.setId("forumdataloader");
			result.add(init);
			Category previous = init;

			for (int i = 0; i < (getMaxCategory()); i++) {
				Category category = newCategory(previous);
				result.add(category);
				previous = category;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	private int getMaxCategory() {
		return (randomize)?rand.nextInt(maxCategories):maxCategories;
	}

	private Forum newForum(Forum previous) {
		if (previous == null) {
			previous = new Forum();
		}
		Forum forum = new Forum();
		forum.setCreatedDate(new Date());
		forum.setDescription(randomWords(10));
		forum.setForumName(randomWords(5));
		forum.setForumOrder(previous.getForumOrder() + 1);
		forum.setOwner("root");
		return forum;
	}

	private Category newCategory(Category previous) {
		if (previous == null) {
			previous = new Category();
		}
		Category category = new Category();
		category.setCategoryName(randomWords(10));
		category.setCategoryOrder(previous.getCategoryOrder() + 1);
		category.setCreatedDate(new Date());
		category.setDescription(randomWords(10));
		category.setModifiedBy("root");
		category.setModifiedDate(new Date());
		category.setOwner("root");
		return category;
	}

	public List<Forum> findForumsByCategory(String categoryId) {

		List<Forum> result = new ArrayList<Forum>();
		try {
			Forum previous = null;
			int forumCount = maxForums();

			for (int i = 0; i < forumCount; i++) {
				Forum forum = newForum(previous);

				result.add(forum);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	private int maxForums() {
		return (randomize)?(rand.nextInt(maxForums) + 1):maxForums;
	}

	public boolean isInitialized() {
		try {
			Category initialized = forumService.getCategory("forumdataloader");
			return (initialized != null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public List<Topic> findTopicsByForum(Forum forum) {
		List<Topic> result = new ArrayList<Topic>();
		try {
			Topic previous = null;
			int topicCount = maxTopics();

			for (int i = 0; i < topicCount; i++) {
				Topic topic = newTopic(previous);

				result.add(topic);
			}
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	private int maxTopics() {
		return (randomize)?(rand.nextInt(maxTopics) + 1):maxTopics;
	}

	private Topic newTopic(Topic previous) {
		if (previous == null) {
			previous = new Topic();
		}
		Topic topic = new Topic();
		topic.setCreatedDate(new Date());
		topic.setDescription(randomWords(10));
		topic.setOwner("root"); // todo use random user
		topic.setTopicName(randomWords(5));
		topic.setIcon("Shield");
		return topic;
	}
	public List<Post> fingPostsByTopic(Topic topic) {
		List<Post> result = new ArrayList<Post>();
		try {
			Post previous = null;
			int postCount = maxPosts();

			for (int i = 0; i < postCount; i++) {
				Post post = newPost(previous);

				result.add(post);

			}
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private int maxPosts() {
		return (randomize)?(rand.nextInt(maxPosts) + 1):maxPosts;
	}

	private Post newPost(Post previous) {
		if (previous == null) {
			previous = new Post();
		}
		Post post = new Post();
		post.setName(randomWords(10));
		String content = randomParagraphs(5);
		
		post.setMessage(content);
		post.setOwner("root");
		post.setIcon("Shield");
		return post;
	}



}
