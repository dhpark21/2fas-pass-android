package com.twofasapp.core.design.feature.items

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.design.LocalDarkMode
import com.twofasapp.core.design.R

@Composable
fun PaymentCardLogo(
    modifier: Modifier = Modifier,
    cardIssuer: ItemContent.PaymentCard.Issuer?,
) {
    Image(
        modifier = modifier,
        painter = painterResource(
            when (cardIssuer) {
                ItemContent.PaymentCard.Issuer.Visa -> if (LocalDarkMode.current) R.drawable.paymentcard_visa_dark else R.drawable.paymentcard_visa
                ItemContent.PaymentCard.Issuer.MasterCard -> R.drawable.paymentcard_mastercard
                ItemContent.PaymentCard.Issuer.AmericanExpress -> R.drawable.paymentcard_amex
                ItemContent.PaymentCard.Issuer.Discover -> R.drawable.paymentcard_discover
                ItemContent.PaymentCard.Issuer.DinersClub -> R.drawable.paymentcard_dinersclub
                ItemContent.PaymentCard.Issuer.Jcb -> R.drawable.paymentcard_jcb
                ItemContent.PaymentCard.Issuer.UnionPay -> R.drawable.paymentcard_unionpay
                null -> R.drawable.img_placeholder
            },
        ),
        contentDescription = null,
    )
}