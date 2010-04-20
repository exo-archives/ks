package org.exoplatform.forum.bench;

import java.text.MessageFormat;
import java.util.List;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Startable;

public class ForumDataInitializer implements Startable {

	private static Log log = ExoLogger.getLogger(ForumDataInitializer.class);
	
	private ForumService forumService;
	private ForumDataProvider provider;
	
	public ForumDataInitializer(InitParams initParams, ForumService forumService,ForumDataProvider provider) {

		this.provider = provider;
		this.forumService = forumService;

	}



	public void start() {
		log.info("Initializing Forum data loader...");
		try {
			initCategories();
		} catch (Exception e) {
			log.error("Failed to init : ", e);
		}
		log.info("Forum data loaded!");
	}



	private void initCategories() throws Exception {
		if (provider.isInitialized()) {
			log.info("Categories seem to be already initialized. Skipping.");
			return;
		}
		
		
		
		long topicsCount = 0;
		long forumsCount = 0;
		long postCount = 0;
		long categoriesWeight = 0;
		List<Category> categories = provider.findCategories();
		long categoriesCount = 0;
		
		for (Category category : categories) {
			forumService.saveCategory( category, true);
			
			categoriesCount++;
			String categoryId = category.getId();
			List<Forum> forums = provider.findForumsByCategory(categoryId);
			log.info("Category " + categoriesCount + "/" + categories.size()+" with " +forums.size()+ " forums");			
			long forumsWeight = 0;
			forumsCount += forums.size();
			int forumNum=0;
			for (Forum forum : forums) {
		
				forumService.saveForum(categoryId, forum, true);

				
				String forumId = forum.getId();
				long topicsWeight = 0;

				List<Topic> topics = provider.findTopicsByForum(forum);
				log.info("\tForum "+ (++forumNum) + "/"+ forums.size() + " with " + topics.size()+ " topics");
				int topicNum = 0;
				for (Topic topic : topics) {
		
					forumService.saveTopic(categoryId, forumId, topic, true, false, "");
					//log.info("Created topic " + topic.getTopicName());
					
					String topicId = topic.getId();
					List<Post> posts = provider.fingPostsByTopic(topic);
					//log.info("Initializing new topic with "+ posts.size()+ " posts");
					postCount += posts.size();
					long postsWeight = 0;
					long t1 = System.currentTimeMillis();
					for (Post post : posts) {
		
						forumService.savePost(categoryId, forumId, topicId, post, true, "");
						long messageWeight = post.getMessage().length()*2; // in bytes
						postsWeight += messageWeight;
	
					}
					double elapsed = (System.currentTimeMillis() - t1);
					double rate = ((postsWeight/1024) / (elapsed/1000));
					String srate = MessageFormat.format("({0,number,#.#} K/s)", rate);
					log.info("\t\tTopic "+ (++topicNum) + "/" + topics.size() + "\t" + posts.size()+ " posts in "+ elapsed + "ms " + srate  );
					
					topicsWeight+=postsWeight;
	
				} // end topics loop

				
				log.info("\t\t "+topics.size()+" topics " +  MessageFormat.format("({0,number,#.#} K)", (topicsWeight/1024)) + " total posts="  + postCount);
				forumsWeight+= topicsWeight;
				topicsCount+=topics.size();
			
			}
			log.info("\t"+forums.size()+" forums " +  MessageFormat.format("({0,number,#.#} K)", (forumsWeight/1024)) + " total posts="  + postCount);
			categoriesWeight+=forumsWeight;

		}

		log.info("INITIALIZED : categories=" + categories.size() + " / forums=" + forumsCount + " / topics=" + topicsCount + " / posts=" + postCount + MessageFormat.format(" ({0,number,#.#} K)", (categoriesWeight/1024)));
	
	}





	public void stop() {
		// TODO Auto-generated method stub

	}

}
