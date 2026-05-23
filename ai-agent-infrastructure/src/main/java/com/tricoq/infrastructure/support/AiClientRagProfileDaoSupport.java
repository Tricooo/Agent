package com.tricoq.infrastructure.support;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tricoq.infrastructure.dao.IAiClientRagProfileDao;
import com.tricoq.infrastructure.dao.po.AiClientRagProfile;
import org.springframework.stereotype.Repository;

@Repository
public class AiClientRagProfileDaoSupport extends ServiceImpl<IAiClientRagProfileDao, AiClientRagProfile> {
}
