package com.coffee4j.model;

import org.bson.types.ObjectId;
import java.util.Set;

public record Schema(ObjectId objectId, ObjectId creatorId, Set<Field> fields, boolean defaultFlag,
                     boolean sharedFlag) {
}