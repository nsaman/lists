package com.lists.config;

import com.lists.handler.CreateElaticIndexesHandler;
import com.lists.handler.CreateThingHandler;
import com.lists.handler.DynamoThingStreamHandler;
import com.lists.handler.SearchThingHandler;
import dagger.Component;

import javax.inject.Singleton;

@Component(modules = {
        Module.class
})
@Singleton
public interface Components {

    DynamoThingStreamHandler dynamoThingStreamHandler();

    CreateElaticIndexesHandler createElaticIndexesHandler();

    CreateThingHandler createThingHandler();

    SearchThingHandler searchThingHandler();
}
