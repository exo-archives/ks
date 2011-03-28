package org.exoplatform.forum.service;

public class SortSettings {

  private SortField field     = SortField.ORDER; // Name , Order, createdDate, laspostDate, postCount, topicCount, isLock, lastPostDate, viewCount, numberAttachments

  private Direction direction = Direction.ASC;  // ascending or descending

  public SortSettings(String field, String direction) {
    this.field = toField(field);
    this.direction = toDirection(direction);
  }

  public SortSettings(SortField field, Direction direction) {
    this.field = field;
    this.direction = direction;
  }

  public SortField getField() {
    return field;
  }

  public Direction getDirection() {
    return direction;
  }

  private SortField toField(String sortBy) {
    if ("name".equalsIgnoreCase(sortBy))
      return SortField.NAME;
    else if ("createdDate".equalsIgnoreCase(sortBy))
      return SortField.CREATED;
    else if ("isLock".equalsIgnoreCase(sortBy))
      return SortField.ISLOCK;
    else if ("topicCount".equalsIgnoreCase(sortBy))
      return SortField.TOPICCOUNT;
    else if ("lastPostDate".equalsIgnoreCase(sortBy))
      return SortField.LASTPOST;
    else if ("postCount".equalsIgnoreCase(sortBy))
      return SortField.POSTCOUNT;
    else if ("viewCount".equalsIgnoreCase(sortBy))
      return SortField.VIEWCOUNT;
    else if ("numberAttachments".equalsIgnoreCase(sortBy))
      return SortField.ATTACHMENTS;
    else
      return SortField.ORDER;
  }

  private Direction toDirection(String direction) {
    return ("descending".equalsIgnoreCase(direction)) ? Direction.DESC : Direction.ASC;
  }

  public enum Direction {
    ASC("ascending"), DESC("descending");
    private final String name;

    Direction(String name) {
      this.name = name;
    }

    public String toString() {
      return name;
    }
  };

  public enum SortField {
    NAME("name"), ORDER("forumOrder"), CREATED("createdDate"), LASTPOST("lastPostDate"), POSTCOUNT("postCount"), TOPICCOUNT("topicCount"), ISLOCK("isLock"), VIEWCOUNT("viewCount"), ATTACHMENTS("numberAttachments");

    private final String name;

    SortField(String name) {
      this.name = name;
    }

    public String toString() {
      return name;
    }
  };

}
