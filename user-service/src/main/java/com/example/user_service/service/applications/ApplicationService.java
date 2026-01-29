package com.example.user_service.service.applications;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.user_service.entity.Application;
import com.example.user_service.exception.BadRequestException;
import com.example.user_service.repository.ApplicationRepository;
import com.example.user_service.utils.AppKey;

@Service
public class ApplicationService {
    private final ApplicationRepository applicationRepository;

    public ApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    public Application createApplication(String name) {
        if (applicationRepository.existsByName(name)) {
            throw new BadRequestException("App with name" + name + " already exist");
        }

        Application app = new Application();

        app.setName(name);
        app.setAppKey(AppKey.generate(name));

        return applicationRepository.save(app);
    }

    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }
}
