package org.telekit.base.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.telekit.base.domain.NamedBean;
import org.telekit.base.domain.TelekitException;

import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isBlank;

public abstract class XMLBeanRepository<T extends NamedBean<T>> {

    protected List<T> repository;
    protected XmlMapper mapper;

    public XMLBeanRepository(XmlMapper mapper) {
        this.mapper = mapper;
        this.repository = load();
    }

    public List<T> getAll() {
        return new ArrayList<>(repository);
    }

    public Optional<T> findById(String id) {
        if (isBlank(id)) return Optional.empty();
        return repository.stream()
                .filter(val -> id.equals(val.getId()))
                .findFirst();
    }

    public Set<String> getNames() {
        return repository.stream()
                .map(T::getName)
                .collect(Collectors.toSet());
    }

    public void create(T bean) {
        requireMandatoryFields(bean);
        requireUniqueFields(bean);
        List<T> backup = getAll();
        try {
            repository.add(bean.deepCopy());
            store();
        } catch (Exception e) {
            repository = backup;
            throw e;
        }
    }

    public void update(T bean) {
        requireMandatoryFields(bean);
        List<T> backup = getAll();
        try {
            if (repository.contains(bean)) {
                repository.remove(bean);
                repository.add(bean.deepCopy());
                store();
            }
        } catch (Exception e) {
            repository = backup;
            throw e;
        }
    }

    public void delete(T bean) {
        Objects.requireNonNull(bean);
        Objects.requireNonNull(bean.getId());
        List<T> backup = getAll();
        try {
            repository.remove(bean);
            store();
        } catch (Exception e) {
            repository = backup;
            throw e;
        }
    }

    public Optional<String> getAsXML(String id) {
        return findById(id).map(bean -> {
            try {
                return mapper.writeValueAsString(bean);
            } catch (Exception e) {
                throw new TelekitException("Unable to serialize object to XML", e);
            }
        });
    }

    public void loadFromXML(String xml) {
        try {
            T bean = deserialize(xml);
            repository.remove(bean);
            repository.add(bean);
            store();
        } catch (Exception e) {
            throw new TelekitException("Unable to parse object from XML", e);
        }
    }

    protected void requireMandatoryFields(T bean) {
        if (bean == null || isBlank(bean.getId()) || isBlank(bean.getName())) {
            throw new TelekitException("Object ID or name is invalid");
        }
    }

    protected void requireUniqueFields(T bean) {
        findById(bean.getId()).ifPresent(found -> {
            throw new TelekitException("Object with ID \"" + bean.getId() + "\" already exists");
        });
    }

    protected abstract T deserialize(String xml) throws Exception;

    protected abstract List<T> load();

    protected abstract void store();
}
