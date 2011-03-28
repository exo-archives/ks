package org.exoplatform.faq.service;

import java.util.HashSet;

import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class QuestionNodeListener implements EventListener {
  private Log log = ExoLogger.getLogger(QuestionNodeListener.class);

  public QuestionNodeListener() {

  }

  @Override
  public void onEvent(EventIterator events) {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    FAQService service = (FAQService) container.getComponentInstanceOfType(FAQService.class);
    try {
      // this map is used to watch an answer node is checked activated and approved properties or not yet.
      HashSet<String> checkedAnswerNodes = new HashSet<String>();
      while (events.hasNext()) {
        Event event = events.nextEvent();
        String pathString = event.getPath();

        if (pathString.substring(0, pathString.length() - 1).indexOf(Utils.COMMENT_HOME) > 0) {
          // if there is a change in comment home node...
          if ((event.getType() == Event.NODE_ADDED || event.getType() == Event.NODE_REMOVED))
            service.reCalculateLastActivityOfQuestion(pathString);
        }

        if (pathString.substring(0, pathString.length() - 3).indexOf(Utils.ANSWER_HOME) > 0) {
          if (event.getType() == Event.NODE_REMOVED) {
            service.reCalculateLastActivityOfQuestion(pathString);
            continue;
          }
          // if there is a change in answer home node...
          if (event.getType() == Event.PROPERTY_ADDED || event.getType() == Event.PROPERTY_CHANGED || event.getType() == Event.PROPERTY_REMOVED) {
            int lastSlash = pathString.lastIndexOf("/");
            String propName = pathString.substring(lastSlash + 1);

            if (propName.equalsIgnoreCase("exo:activateResponses") || propName.equalsIgnoreCase("exo:approveResponses")) {
              // if activate or approve properties of an answer was changed.
              String answerNodePath = pathString.substring(0, lastSlash);
              if (!checkedAnswerNodes.contains(answerNodePath)) {
                checkedAnswerNodes.add(answerNodePath);
                service.reCalculateLastActivityOfQuestion(pathString);
              }
            }
          }
        }

      }
    } catch (Exception re) {
      log.error("can not update last activity of question", re);
    }

  }

}
