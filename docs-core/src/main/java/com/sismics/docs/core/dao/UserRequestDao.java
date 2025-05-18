package com.sismics.docs.core.dao;

import com.sismics.docs.core.dao.dto.UserRequestDto;
import com.sismics.docs.core.model.jpa.UserRequest;
import com.sismics.util.context.ThreadLocalContext;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserRequestDao {
    /**
     * Creates a new user request.
     *
     * @param userRequest User request to create
     * @return ID of the created user request
     */
    public String create(UserRequest userRequest) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();

        if (userRequest.getId() == null) {
            userRequest.setId(UUID.randomUUID().toString());
        }

        em.persist(userRequest);

        return userRequest.getId();
    }

    /**
     * Returns the list of all user requests.
     *
     * @return List of user requests
     */
    public List<UserRequestDto> findAll() {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        StringBuilder sb = new StringBuilder("select ur.URQ_ID_C, ur.URQ_USERNAME_C, ur.URQ_EMAIL_C, ur.URQ_CREATEDATE_D, ur.URQ_STATUS_C ")
                .append(" from T_USER_REQUEST ur ")
                .append(" order by ur.URQ_CREATEDATE_D desc");
        Query q = em.createNativeQuery(sb.toString());

        @SuppressWarnings("unchecked")
        List<Object[]> resultList = q.getResultList();
        List<UserRequestDto> userRequestDtoList = new ArrayList<>();

        for (Object[] result : resultList) {
            int i = 0;
            UserRequestDto userRequestDto = new UserRequestDto();
            userRequestDto.setId((String) result[i++]);
            userRequestDto.setUsername((String) result[i++]);
            userRequestDto.setEmail((String) result[i++]);
            userRequestDto.setCreateDate((java.util.Date) result[i++]);
            userRequestDto.setStatus((String) result[i]);
            userRequestDtoList.add(userRequestDto);
        }

        return userRequestDtoList;
    }

    /**
     * Returns a user request by ID.
     *
     * @param id User request ID
     * @return User request
     */
    public UserRequest findById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        TypedQuery<UserRequest> q = em.createQuery("select ur from UserRequest ur where ur.id = :id", UserRequest.class);
        q.setParameter("id", id);
        List<UserRequest> results = q.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Updates a user request status.
     *
     * @param id User request ID
     * @param status New status
     */
    public void updateStatus(String id, String status) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        UserRequest userRequest = em.find(UserRequest.class, id);
        if (userRequest != null) {
            userRequest.setStatus(status);
        }
    }

    /**
     * Check if a username already exists in user requests or regular users.
     *
     * @param username Username to check
     * @return True if the username exists
     */
    public boolean isUsernameExists(String username) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();

        // Check in user requests
        TypedQuery<Long> q1 = em.createQuery("select count(ur) from UserRequest ur where ur.username = :username", Long.class);
        q1.setParameter("username", username);
        Long count1 = q1.getSingleResult();

        if (count1 > 0) {
            return true;
        }

        // Check in regular users
        TypedQuery<Long> q2 = em.createQuery("select count(u) from User u where u.username = :username and u.deleteDate is null", Long.class);
        q2.setParameter("username", username);
        Long count2 = q2.getSingleResult();

        return count2 > 0;
    }
}