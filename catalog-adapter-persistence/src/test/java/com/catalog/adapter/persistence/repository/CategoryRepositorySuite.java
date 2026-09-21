package com.catalog.adapter.persistence.repository;

import com.catalog.adapter.persistence.adapter.CategoryJpaRetrievingDelegateImplTest;
import com.catalog.adapter.persistence.adapter.CategoryNodeRepositoryImplTest;
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
