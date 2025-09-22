package com.example.studentcourse.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import com.example.studentcourse.model.Subject;

@Aspect
@Component
public class RepoAspect {

    @Around("execution(* org.springframework.data.repository.CrudRepository+.save(..)) && args(entity)")
    public Object aroundSave(ProceedingJoinPoint pjp, Object entity) throws Throwable {
        if (entity instanceof Subject) {
            Subject s = (Subject) entity;
            System.out.println("DEBUG RepoAspect: saving Subject -> nome=\"" + s.getNome() + "\" codigo=\"" + s.getCodigo() + "\"");
            new Exception("StackTrace - who called save(Subject)").printStackTrace(System.out);
        }
        return pjp.proceed();
    }
}
