package com.example.userservice.dao.impl;

import com.example.userservice.dao.UserDao;
import com.example.userservice.entity.User;
import com.example.userservice.exception.DaoException;
import com.example.userservice.util.HibernateUtil;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class UserDaoImpl implements UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDaoImpl.class);

    @Override
    public User save(User user) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(user);
            tx.commit();
            log.debug("Создан пользователь: {}", user);
            return user;
        } catch (HibernateException e) {
            rollback(tx);
            log.error("Ошибка сохранения пользователя: {}", user, e);
            throw new DaoException("Не удалось сохранить пользователя", e);
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = session.get(User.class, id);
            return Optional.ofNullable(user);
        } catch (HibernateException e) {
            log.error("Ошибка поиска пользователя по id={}", id, e);
            throw new DaoException("Не удалось найти пользователя", e);
        }
    }

    @Override
    public List<User> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from User order by id", User.class).list();
        } catch (HibernateException e) {
            log.error("Ошибка получения списка пользователей", e);
            throw new DaoException("Не удалось получить список пользователей", e);
        }
    }

    @Override
    public User update(User user) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            User merged = session.merge(user);
            tx.commit();
            log.debug("Обновлён пользователь: {}", merged);
            return merged;
        } catch (HibernateException e) {
            rollback(tx);
            log.error("Ошибка обновления пользователя: {}", user, e);
            throw new DaoException("Не удалось обновить пользователя", e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            User user = session.get(User.class, id);
            if (user == null) {
                tx.commit();
                return false;
            }
            session.remove(user);
            tx.commit();
            log.debug("Удалён пользователь id={}", id);
            return true;
        } catch (HibernateException e) {
            rollback(tx);
            log.error("Ошибка удаления пользователя id={}", id, e);
            throw new DaoException("Не удалось удалить пользователя", e);
        }
    }

    private void rollback(Transaction tx) {
        if (tx != null && tx.isActive()) {
            try {
                tx.rollback();
            } catch (HibernateException ex) {
                log.warn("Не удалось откатить транзакцию", ex);
            }
        }
    }
}