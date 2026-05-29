package com.catalog.infrastructure.repository.jpa;

import com.catalog.infrastructure.repository.jpa.adapter.CategoryJpaRetrievingDelegateImplTest;
import com.catalog.infrastructure.repository.jpa.impl.CategoryNodeRepositoryImplTest;
import org.junit.platform.suite.api.*;

@Suite
@SuiteDisplayName("Category Infrastructure Suite")
@SelectClasses({
        CategoryJpaRetrievingDelegateImplTest.class,
        CategoryNodeRepositoryImplTest.class,
        CategoryQueryRepositoryTest.class,
})
public class CategoryRepositorySuite {
}
