package org.jacorb.collection;

interface IteratorFactory{
    PositionalIteratorImpl create_iterator( CollectionImpl collection, boolean read_only );
    PositionalIteratorImpl create_iterator( CollectionImpl collection, boolean read_only, boolean reverse );
