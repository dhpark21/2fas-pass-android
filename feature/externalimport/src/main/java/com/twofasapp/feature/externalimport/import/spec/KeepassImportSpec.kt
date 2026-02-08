/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.externalimport.import.spec

import android.content.Context
import android.net.Uri
import com.twofasapp.core.common.domain.ImportType
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.ItemContentType
import com.twofasapp.core.common.ktx.readTextFile
import com.twofasapp.core.locale.R
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.feature.externalimport.import.CsvParser
import com.twofasapp.feature.externalimport.import.ImportContent

internal class KeepassImportSpec(
    vaultsRepository: VaultsRepository,
    private val context: Context,
) : AbstractKeepassImportSpec(vaultsRepository, context) {

    companion object {
        private const val CSV_ROW_ACCOUNT = "Account"
        private const val CSV_ROW_LOGIN_NAME = "Login Name"
        private const val CSV_ROW_PASSWORD = "Password"
        private const val CSV_ROW_WEB_SITE = "Web Site"
        private const val CSV_ROW_COMMENTS = "Comments"
    }

    override val type = ImportType.KeePass
    override val name = ImportType.KeePass.displayName
    override val image = com.twofasapp.core.design.R.drawable.external_logo_keepass
    override val instructions = context.getString(R.string.transfer_instructions_keepass)
    override val additionalInfo = null
    override val cta: List<Cta> = listOf(
        Cta.Primary(
            text = context.getString(R.string.transfer_instructions_cta_keepass),
            action = CtaAction.ChooseFile,
        ),
    )

    override val xmlTagSeparator = ";"

    override fun parseCsv(uri: Uri, vaultId: String): ImportContent {
        val items = buildList {
            CsvParser.parse(
                text = context.readTextFile(uri),
            ) { row ->
                add(
                    Item.create(
                        vaultId = vaultId,
                        contentType = ItemContentType.Login,
                        content = ItemContent.Login.create(
                            name = row.get(CSV_ROW_ACCOUNT),
                            username = row.get(CSV_ROW_LOGIN_NAME),
                            password = row.get(CSV_ROW_PASSWORD),
                            url = row.get(CSV_ROW_WEB_SITE),
                            notes = row.get(CSV_ROW_COMMENTS),
                        ),
                    ),
                )
            }
        }

        return ImportContent(
            items = items,
            tags = emptyList(),
            unknownItems = 0,
        )
    }

    override fun parseXmlDate(date: String): Long? {
        return parseDate(date)
    }
}