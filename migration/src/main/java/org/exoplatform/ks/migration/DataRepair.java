package org.exoplatform.ks.migration;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Value;

import org.exoplatform.faq.service.Answer;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.picocontainer.Startable;

public class DataRepair implements Startable{
	private ForumService forumService_ ; 
	private FAQService faqService_ ;
	
	public DataRepair(FAQService faqService, ForumService forumService) throws Exception {
		forumService_ = forumService ;
		faqService_ = faqService ;
	}

	public void start() {
		SessionProvider sysSession = SessionProvider.createSystemProvider() ;
    long item = 0 ;
    long size = 0 ; 
		try{
			//faqService_.getAllQuestions(sysSession). ;
			NodeIterator questionIter = faqService_.getQuestionsIterator(sysSession) ;
      size = questionIter.getSize() ;                  
      System.out.println("\n ==> Found "+ size + " data item(s) to migrate in FAQ ");
      faqService_.removeRSSEventListener() ;      
			Node questionNode ;
			Value[] responses ;
			Value[] dateOfResponses ;
			Value[] responseBy ;
      while(questionIter.hasNext()) {
				questionNode = questionIter.nextNode() ;
        item ++ ;
				System.out.println(">>>>>> ["+item+"/"+size+"] Repairing question: " + questionNode.getProperty("exo:name").getString()) ;
				if(questionNode.hasProperty("exo:responses")) {
					try{
						responses = questionNode.getProperty("exo:responses").getValues() ;
						dateOfResponses = questionNode.getProperty("exo:dateResponse").getValues() ;
						responseBy = questionNode.getProperty("exo:responseBy").getValues() ;
						if(responses.length > 0) {
							List<Answer> answers = new ArrayList<Answer>() ;
							Answer ans; 
							for(int i = 0; i < responses.length; i++) {
								//System.out.println("AnswerBy: " + responseBy[i].getString()) ;
								ans = new Answer(responseBy[i].getString(), true) ;
								ans.setResponses(responses[i].getString()) ;
								ans.setDateResponse(dateOfResponses[i].getDate().getTime()) ;
								ans.setNew(true);
								answers.add(ans) ;
							}
							faqService_.saveAnswer(questionNode.getName(), answers.toArray(new Answer[]{}), sysSession) ;
						}
						questionNode.setProperty("exo:title", questionNode.getProperty("exo:name").getString()) ;
						//questionNode.setProperty("exo:responses", (Value[])null) ;
						//questionNode.setProperty("exo:responseBy", (Value[])null) ;
						//questionNode.setProperty("exo:dateResponse", (Value[])null);
						questionNode.save() ;
					}catch(Exception e) {
						e.printStackTrace() ;
					}				
				}
			}	
      if(size > 0)faqService_.addRSSEventListener() ;		
		}catch(Exception e) {
			e.printStackTrace() ;			
		}finally{
			sysSession.close() ;
		}
		System.out.println("\n ==> "+item+"/"+size+" data item(s) repaired succesful !!! \n");		
	}

	public void stop() {
		// TODO Auto-generated method stub
		
	}
}
