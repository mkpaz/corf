package org.telekit.base.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.telekit.base.OrdinaryTest;
import org.telekit.base.domain.exception.TelekitException;
import org.telekit.base.service.FileBasedRepository.Transaction;
import org.telekit.base.util.UUIDHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@OrdinaryTest
@SuppressWarnings("OptionalGetWithoutIsPresent")
@TestMethodOrder(MethodOrderer.DisplayName.class)
public class FileBasedRepositoryTest {

    public final FooFileBasedRepository fooRepository;

    public FileBasedRepositoryTest() {
        this.fooRepository = new FooFileBasedRepository();
    }

    @Test
    @DisplayName("get all entities fromInt not empty repository and assert all present")
    public void getAll_RepositoryContainsEntities_AllPresent() {
        FooEntity entity1 = new FooEntity(UUIDHelper.fromInt(1));
        FooEntity entity2 = new FooEntity(UUIDHelper.fromInt(2));
        FooEntity entity3 = new FooEntity(UUIDHelper.fromInt(3));
        fooRepository.add(List.of(entity1, entity2, entity3));
        assertThat(fooRepository.getAll()).containsAll(List.of(entity1, entity2, entity3));
        assertThat(fooRepository.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("get all entities fromInt empty repository and assert there are none")
    public void getAll_RepositoryDoesNotContainEntities_IsEmpty() {
        assertThat(fooRepository.getAll()).isNotNull();
        assertThat(fooRepository.getAll()).isEmpty();
    }

    @Test
    @DisplayName("find existing entity by id and assert it's present")
    public void findById_ExistingEntity_IsPresent() {
        FooEntity entity1 = new FooEntity(UUIDHelper.fromInt(1));
        FooEntity entity2 = new FooEntity(UUIDHelper.fromInt(2));
        fooRepository.add(List.of(entity1, entity2));
        assertThat(fooRepository.findById(UUIDHelper.fromInt(1))).isPresent();
    }

    @Test
    @DisplayName("find non-existing entity by id and assert it's not present")
    public void findById_NonExistingEntity_IsPresent() {
        FooEntity entity1 = new FooEntity(UUIDHelper.fromInt(1));
        FooEntity entity2 = new FooEntity(UUIDHelper.fromInt(2));
        fooRepository.add(List.of(entity1, entity2));
        assertThat(fooRepository.findById(UUIDHelper.fromInt(10))).isNotPresent();
    }

    @Test
    @DisplayName("find existing entity and assert it's present")
    public void find_ExistingEntity_IsPresent() {
        FooEntity entity1 = new FooEntity(UUIDHelper.fromInt(1));
        FooEntity entity2 = new FooEntity(UUIDHelper.fromInt(2));
        fooRepository.add(List.of(entity1, entity2));
        assertThat(fooRepository.find(entity1)).isPresent();
    }

    @Test
    @DisplayName("find non-existing entity and assert it's not present")
    public void find_NonExistingEntity_IsPresent() {
        FooEntity entity1 = new FooEntity(UUIDHelper.fromInt(1));
        FooEntity entity2 = new FooEntity(UUIDHelper.fromInt(2));
        fooRepository.add(List.of(entity1, entity2));
        assertThat(fooRepository.find(new FooEntity(UUIDHelper.fromInt(10)))).isNotPresent();
    }

    @Test
    @DisplayName("assert repository contains only existing entities")
    public void contains_ExistingEntity_IsPresent() {
        FooEntity entity1 = new FooEntity(UUIDHelper.fromInt(1));
        FooEntity entity2 = new FooEntity(UUIDHelper.fromInt(2));
        fooRepository.add(List.of(entity1, entity2));
        assertThat(fooRepository.containsId(UUIDHelper.fromInt(1))).isTrue();
        assertThat(fooRepository.containsId(UUIDHelper.fromInt(10))).isFalse();
    }

    @Test
    @DisplayName("add entity with valid ID and check it was added properly")
    public void add_ValidEntity_Added() {
        FooEntity entity = new FooEntity();
        fooRepository.add(entity);
        assertThat(fooRepository.getAll()).contains(entity);
        assertThat(fooRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("add entity with null ID and assert exception thrown")
    public void add_InvalidEntityWithNullID_ExceptionThrown() {
        assertThatThrownBy(() -> {
            FooEntity invalidEntity = new FooEntity();
            invalidEntity.setId(null);
            fooRepository.add(List.of(invalidEntity));
        }).isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("add entity with duplicate ID and assert exception thrown")
    public void add_EntityDuplicateID_ExceptionThrown() {
        FooEntity entity1 = new FooEntity(UUIDHelper.fromInt(1));
        FooEntity entity2 = new FooEntity(UUIDHelper.fromInt(1));
        fooRepository.add(entity1);
        assertThatThrownBy(() -> fooRepository.add(entity2)).isInstanceOf(TelekitException.class);
        assertThat(fooRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("add multiple entities with correct IDs and assert they were added properly")
    public void add_MultipleValidEntities_Added() {
        FooEntity entity1 = new FooEntity(UUIDHelper.fromInt(1));
        FooEntity entity2 = new FooEntity(UUIDHelper.fromInt(2));
        FooEntity entity3 = new FooEntity(UUIDHelper.fromInt(3));
        fooRepository.add(List.of(entity1, entity2, entity3));
        assertThat(fooRepository.getAll()).containsAll(List.of(entity1, entity2, entity3));
        assertThat(fooRepository.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("add multiple entities with duplicate IDs and assert duplicates were ignored")
    public void add_MultipleEntitiesDuplicateKeys_DuplicatesIgnored() {
        FooEntity entity1 = new FooEntity(UUIDHelper.fromInt(1));
        FooEntity entity2 = new FooEntity(UUIDHelper.fromInt(1));
        FooEntity entity3 = new FooEntity(UUIDHelper.fromInt(2));
        FooEntity entity4 = new FooEntity(UUIDHelper.fromInt(1));
        fooRepository.add(List.of(entity1, entity2, entity3, entity4));
        assertThat(fooRepository.getAll()).containsAll(List.of(entity1, entity3));
        assertThat(fooRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("update multiple existing entities and assert they were updated properly")
    public void update_ExistingEntity_Updated() {
        FooEntity entity1 = new FooEntity(UUIDHelper.fromInt(1), "ent_1");
        FooEntity entity2 = new FooEntity(UUIDHelper.fromInt(2), "ent_2");
        FooEntity entity3 = new FooEntity(UUIDHelper.fromInt(3), "ent_3");
        fooRepository.add(List.of(entity1, entity2, entity3));

        FooEntity entity1Upd = entity1.deepCopy();
        entity1Upd.setName("upd_1");
        fooRepository.update(entity1Upd);

        FooEntity entity2Upd = entity2.deepCopy();
        entity2Upd.setName("upd_2");
        fooRepository.update(entity2Upd);

        assertThat(fooRepository.getAll()).containsAll(List.of(entity1Upd, entity2Upd, entity3));
        assertThat(fooRepository.count()).isEqualTo(3);
        assertThat(fooRepository.findById(UUIDHelper.fromInt(1))).get()
                .hasFieldOrPropertyWithValue("name", "upd_1");
        assertThat(fooRepository.findById(UUIDHelper.fromInt(2))).get()
                .hasFieldOrPropertyWithValue("name", "upd_2");
        assertThat(fooRepository.findById(UUIDHelper.fromInt(3))).get()
                .hasFieldOrPropertyWithValue("name", "ent_3");
    }

    @Test
    @DisplayName("update entity with null ID and assert exception thrown")
    public void update_InvalidEntityWithNullID_ExceptionThrown() {
        assertThatThrownBy(() -> {
            FooEntity invalidEntity = new FooEntity();
            invalidEntity.setId(null);
            fooRepository.update(invalidEntity);
        }).isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("update non-existing and assert nothing has changed")
    public void update_NotExistingEntity_NotUpdated() {
        FooEntity entity1 = new FooEntity(UUIDHelper.fromInt(1), "ent_1");
        FooEntity entity2 = new FooEntity(UUIDHelper.fromInt(2), "ent_2");
        fooRepository.add(List.of(entity1, entity2));

        fooRepository.update(new FooEntity(UUIDHelper.fromInt(3), "ent_3"));

        assertThat(fooRepository.getAll()).containsAll(List.of(entity1, entity2));
        assertThat(fooRepository.count()).isEqualTo(2);
        assertThat(fooRepository.findById(UUIDHelper.fromInt(1))).get()
                .hasFieldOrPropertyWithValue("name", "ent_1");
        assertThat(fooRepository.findById(UUIDHelper.fromInt(2))).get()
                .hasFieldOrPropertyWithValue("name", "ent_2");
        assertThat(fooRepository.findById(UUIDHelper.fromInt(3))).isNotPresent();
    }

    @Test
    @DisplayName("remove existing entity by ID and assert it was removed")
    public void removeById_ExistingEntity_Removed() {
        FooEntity entity1 = new FooEntity(UUIDHelper.fromInt(1), "ent_1");
        FooEntity entity2 = new FooEntity(UUIDHelper.fromInt(2), "ent_2");
        FooEntity entity3 = new FooEntity(UUIDHelper.fromInt(3), "ent_3");
        fooRepository.add(List.of(entity1, entity2, entity3));

        fooRepository.removeById(UUIDHelper.fromInt(2));
        assertThat(fooRepository.findById(UUIDHelper.fromInt(2))).isNotPresent();
        assertThat(fooRepository.getAll()).containsAll(List.of(entity1, entity3));
        assertThat(fooRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("remove non-existing entity by ID and assert nothing changed")
    public void removeById_NonExistingEntity_NotRemoved() {
        FooEntity entity1 = new FooEntity(UUIDHelper.fromInt(1), "ent_1");
        FooEntity entity2 = new FooEntity(UUIDHelper.fromInt(2), "ent_2");
        fooRepository.add(List.of(entity1, entity2));

        fooRepository.removeById(UUIDHelper.fromInt(3));
        assertThat(fooRepository.findById(UUIDHelper.fromInt(3))).isNotPresent();
        assertThat(fooRepository.getAll()).containsAll(List.of(entity1, entity2));
        assertThat(fooRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("remove multiple entities by ID and assert only existing were removed")
    public void removeById_MultipleEntities_SomeRemoved() {
        FooEntity entity1 = new FooEntity(UUIDHelper.fromInt(1), "ent_1");
        FooEntity entity2 = new FooEntity(UUIDHelper.fromInt(2), "ent_2");
        FooEntity entity3 = new FooEntity(UUIDHelper.fromInt(3), "ent_3");
        FooEntity entity4 = new FooEntity(UUIDHelper.fromInt(4), "ent_4");
        fooRepository.add(List.of(entity1, entity2, entity3, entity4));

        fooRepository.removeById(UUIDHelper.fromArray(1, 4, 10, 11));

        assertThat(fooRepository.getAll()).containsAll(List.of(entity2, entity3));
        assertThat(fooRepository.count()).isEqualTo(2);
        assertThat(fooRepository.findById(UUIDHelper.fromInt(1))).isNotPresent();
        assertThat(fooRepository.findById(UUIDHelper.fromInt(4))).isNotPresent();
    }

    @Test
    @DisplayName("remove existing entity and assert it was removed")
    public void remove_ExistingEntity_Removed() {
        FooEntity entity1 = new FooEntity(UUIDHelper.fromInt(1), "ent_1");
        FooEntity entity2 = new FooEntity(UUIDHelper.fromInt(2), "ent_2");
        FooEntity entity3 = new FooEntity(UUIDHelper.fromInt(3), "ent_3");
        fooRepository.add(List.of(entity1, entity2, entity3));

        fooRepository.remove(entity2);
        assertThat(fooRepository.findById(UUIDHelper.fromInt(2))).isNotPresent();
        assertThat(fooRepository.getAll()).containsAll(List.of(entity1, entity3));
        assertThat(fooRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("remove non-existing entity and assert nothing has changed")
    public void remove_NonExistingEntity_Removed() {
        FooEntity entity1 = new FooEntity(UUIDHelper.fromInt(1), "ent_1");
        FooEntity entity2 = new FooEntity(UUIDHelper.fromInt(2), "ent_2");
        fooRepository.add(List.of(entity1, entity2));

        fooRepository.remove(new FooEntity(UUIDHelper.fromInt(3), "ent_3"));

        assertThat(fooRepository.findById(UUIDHelper.fromInt(3))).isNotPresent();
        assertThat(fooRepository.getAll()).containsAll(List.of(entity1));
        assertThat(fooRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("remove multiple entities and assert only existing were removed")
    public void remove_MultipleEntities_SomeRemoved() {
        FooEntity entity1 = new FooEntity(UUIDHelper.fromInt(1), "ent_1");
        FooEntity entity2 = new FooEntity(UUIDHelper.fromInt(2), "ent_2");
        FooEntity entity3 = new FooEntity(UUIDHelper.fromInt(3), "ent_3");
        FooEntity entity4 = new FooEntity(UUIDHelper.fromInt(4), "ent_4");
        fooRepository.add(List.of(entity1, entity2, entity3, entity4));

        fooRepository.remove(List.of(
                entity1,
                entity4,
                new FooEntity(UUIDHelper.fromInt(10), "ent_10"),
                new FooEntity(UUIDHelper.fromInt(11), "ent_11")
        ));

        assertThat(fooRepository.getAll()).containsAll(List.of(entity2, entity3));
        assertThat(fooRepository.count()).isEqualTo(2);
        assertThat(fooRepository.findById(entity1.getId())).isNotPresent();
        assertThat(fooRepository.findById(entity1.getId())).isNotPresent();
    }

    @Test
    @DisplayName("clear not empty repository and assert all cleared")
    public void clear_NotEmptyRepository_Cleared() {
        FooEntity entity1 = new FooEntity(UUIDHelper.fromInt(1), "ent_1");
        FooEntity entity2 = new FooEntity(UUIDHelper.fromInt(2), "ent_2");
        FooEntity entity3 = new FooEntity(UUIDHelper.fromInt(3), "ent_3");
        fooRepository.add(List.of(entity1, entity2, entity3));

        fooRepository.clear();
        assertThat(fooRepository.getAll()).isEmpty();
    }

    @Test
    @DisplayName("load data and assert repository updated")
    public void load_FakeSerializer_Loaded() {
        FooDummySerializer serializer = new FooDummySerializer();
        FooEntity entity1 = new FooEntity(UUIDHelper.fromInt(1), "ent_1");
        FooEntity entity2 = new FooEntity(UUIDHelper.fromInt(2), "ent_2");
        FooEntity entity3 = new FooEntity(UUIDHelper.fromInt(3), "ent_3");
        serializer.setData(Set.of(entity1, entity2, entity3));

        fooRepository.load(new ByteArrayInputStream(new byte[32]), serializer);

        assertThat(fooRepository.getAll()).containsAll(Set.of(entity1, entity2, entity3));
        assertThat(fooRepository.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("save repository content and assert data saved")
    public void save_FakeSerializer_Loaded() {
        FooEntity entity1 = new FooEntity(UUIDHelper.fromInt(1), "ent_1");
        FooEntity entity2 = new FooEntity(UUIDHelper.fromInt(2), "ent_2");
        FooEntity entity3 = new FooEntity(UUIDHelper.fromInt(3), "ent_3");
        fooRepository.add(List.of(entity1, entity2, entity3));

        FooDummySerializer serializer = new FooDummySerializer();
        fooRepository.save(new ByteArrayOutputStream(), serializer);

        assertThat(serializer.getData()).containsAll(List.of(entity1, entity2, entity3));
        assertThat(serializer.getData().size()).isEqualTo(3);
    }

    @Test
    @DisplayName("add entities within transaction and assert repository content was updated")
    public void transaction_AddEntities_Updated() {
        FooEntity entity1 = new FooEntity(UUIDHelper.fromInt(1), "ent_1");
        FooEntity entity2 = new FooEntity(UUIDHelper.fromInt(2), "ent_2");
        FooEntity entity3 = new FooEntity(UUIDHelper.fromInt(3), "ent_3");
        fooRepository.add(List.of(entity1));

        Transaction transaction = fooRepository.beginTransaction(false);
        try {
            fooRepository.add(List.of(entity2, entity3));
        } catch (Throwable t) {
            transaction.rollback();
        }

        assertThat(fooRepository.getAll()).containsAll(Set.of(entity1, entity2, entity3));
        assertThat(fooRepository.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("add entities within transaction then trigger rollback and assert repository content was not updated")
    public void transactionRollback_AddEntities_NoyUpdated() {
        FooEntity entity1 = new FooEntity(UUIDHelper.fromInt(1), "ent_1");
        FooEntity entity2 = new FooEntity(UUIDHelper.fromInt(2), "ent_2");
        FooEntity entity3 = new FooEntity(UUIDHelper.fromInt(3), "ent_3");
        fooRepository.add(List.of(entity1));

        Transaction transaction = fooRepository.beginTransaction(false);
        try {
            fooRepository.add(List.of(entity2, entity3));
            throw new RuntimeException("Trigger rollback");
        } catch (Throwable t) {
            transaction.rollback();
        }

        assertThat(fooRepository.getAll()).containsOnly(entity1);
        assertThat(fooRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("update entity within transaction and assert repository content was updated")
    public void transactionRollback_UpdateEntities_Updated() {
        FooEntity entity1 = new FooEntity(UUIDHelper.fromInt(1), "ent_1");
        FooEntity entity2 = new FooEntity(UUIDHelper.fromInt(2), "ent_2");
        FooEntity entity3 = new FooEntity(UUIDHelper.fromInt(3), "ent_3");
        fooRepository.add(List.of(entity1, entity2, entity3));

        FooEntity entity2Upd = entity2.deepCopy();
        entity2Upd.setName("ent_2_updated");

        Transaction transaction = fooRepository.beginTransaction(entity2);
        try {
            fooRepository.update(entity2Upd);
            throw new RuntimeException("Trigger rollback");
        } catch (Throwable t) {
            transaction.rollback();
        }

        assertThat(fooRepository.findById(UUIDHelper.fromInt(2)).get().getName()).isEqualTo("ent_2");
        assertThat(fooRepository.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("update entity within transaction then trigger rollback and assert repository content was not updated")
    public void transactionRollback_UpdateEntities_NotUpdated() {
        FooEntity entity1 = new FooEntity(UUIDHelper.fromInt(1), "ent_1");
        FooEntity entity2 = new FooEntity(UUIDHelper.fromInt(2), "ent_2");
        FooEntity entity3 = new FooEntity(UUIDHelper.fromInt(3), "ent_3");
        fooRepository.add(List.of(entity1, entity2, entity3));

        FooEntity entity2Upd = entity2.deepCopy();
        entity2Upd.setName("ent_2_updated");

        Transaction transaction = fooRepository.beginTransaction(entity2);
        try {
            fooRepository.update(entity2Upd);
            throw new RuntimeException("Trigger rollback");
        } catch (Throwable t) {
            transaction.rollback();
        }

        assertThat(fooRepository.findById(UUIDHelper.fromInt(2)).get().getName()).isEqualTo("ent_2");
        assertThat(fooRepository.count()).isEqualTo(3);
    }

    public static class FooFileBasedRepository extends FileBasedRepository<FooEntity, UUID> {}

    public static class FooDummySerializer implements Serializer<Collection<FooEntity>> {

        private Collection<FooEntity> data;

        public Collection<FooEntity> getData() {
            return data;
        }

        public void setData(Collection<FooEntity> data) {
            this.data = data;
        }

        @Override
        public void serialize(OutputStream outputStream, Collection<FooEntity> value) {
            this.data = new HashSet<>(value);
        }

        @Override
        public Collection<FooEntity> deserialize(InputStream inputStream) {
            return data;
        }
    }
}